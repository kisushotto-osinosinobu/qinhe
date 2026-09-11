package com.retailable.supermarket;

import com.retailable.supermarket.catalog.ProductMapper;
import com.retailable.supermarket.common.BusinessException;
import com.retailable.supermarket.inventory.InventoryMapper;
import com.retailable.supermarket.inventory.InventoryService;
import com.retailable.supermarket.order.OrderDtos;
import com.retailable.supermarket.order.OrderService;
import com.retailable.supermarket.purchase.PurchaseDtos;
import com.retailable.supermarket.purchase.PurchaseService;
import com.retailable.supermarket.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class BusinessLifecycleIT {
    @Autowired ProductMapper products;
    @Autowired InventoryMapper inventories;
    @Autowired InventoryService inventory;
    @Autowired PurchaseService purchases;
    @Autowired OrderService orders;

    private final UserPrincipal member = new UserPrincipal(4L,"member","","演示会员","MEMBER",0,true);
    private final UserPrincipal cashier = new UserPrincipal(3L,"cashier","","收银员","CASHIER",0,true);

    @Test
    void purchaseSaleCancelPaymentAndReturnRemainConsistent() {
        long productId = newProduct("flow");
        var purchase = purchases.create(new PurchaseDtos.PurchaseRequest(1L,"integration",
                List.of(new PurchaseDtos.PurchaseLine(productId,10,new BigDecimal("3.50")))),1L);
        long purchaseId=((Number)purchase.get("id")).longValue();
        purchases.confirm(purchaseId,1L);
        purchases.confirm(purchaseId,1L);
        assertThat(inventories.lock(productId).getCurrentQty()).isEqualTo(10);

        String pendingKey="cancel-"+UUID.randomUUID();
        var pending=orders.create(new OrderDtos.CreateOrderRequest(pendingKey,List.of(new OrderDtos.OrderLine(productId,3)),false,null),member);
        long pendingId=((Number)pending.get("id")).longValue();
        var duplicatePending=orders.create(new OrderDtos.CreateOrderRequest(pendingKey,List.of(new OrderDtos.OrderLine(productId,3)),false,null),member);
        assertThat(((Number)duplicatePending.get("id")).longValue()).isEqualTo(pendingId);
        assertThat(inventories.lock(productId).getReservedQty()).isEqualTo(3);
        orders.cancel(pendingId,member);orders.cancel(pendingId,member);
        assertThat(inventories.lock(productId).getReservedQty()).isZero();

        var paid=orders.create(new OrderDtos.CreateOrderRequest("paid-"+UUID.randomUUID(),List.of(new OrderDtos.OrderLine(productId,4)),true,"CASH"),cashier);
        long orderId=((Number)paid.get("id")).longValue();
        assertThat(inventories.lock(productId).getCurrentQty()).isEqualTo(6);
        orders.pay(orderId,new OrderDtos.PayRequest("pay-repeat-"+UUID.randomUUID(),"CASH"),cashier);
        assertThat(inventories.lock(productId).getCurrentQty()).isEqualTo(6);
        @SuppressWarnings("unchecked") var items=(List<Map<String,Object>>)paid.get("items");
        long saleItemId=((Number)items.get(0).get("id")).longValue();
        String returnKey="return-"+UUID.randomUUID();
        var returnRequest=new OrderDtos.ReturnRequest(returnKey,"包装完好",List.of(new OrderDtos.ReturnLine(saleItemId,2)));
        orders.returnGoods(orderId,returnRequest,cashier);
        orders.returnGoods(orderId,returnRequest,cashier);
        assertThat(inventories.lock(productId).getCurrentQty()).isEqualTo(8);
        assertThatThrownBy(()->orders.returnGoods(orderId,new OrderDtos.ReturnRequest("too-much-"+UUID.randomUUID(),"超额",List.of(new OrderDtos.ReturnLine(saleItemId,3))),cashier)).isInstanceOf(BusinessException.class);
    }

    @Test
    void concurrentOrdersCannotOversellLastUnit() throws Exception {
        long productId=newProduct("race");inventory.manualMovement(productId,1,true,1L,"并发测试初始化");
        assertThatThrownBy(()->inventory.manualMovement(productId,2,false,1L,"库存不足拒绝"))
                .isInstanceOf(BusinessException.class);
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch ready=new CountDownLatch(2),go=new CountDownLatch(1);
        Callable<Boolean> task=()->{ready.countDown();go.await(10,TimeUnit.SECONDS);try{orders.create(new OrderDtos.CreateOrderRequest("race-"+UUID.randomUUID(),List.of(new OrderDtos.OrderLine(productId,1)),false,null),member);return true;}catch(BusinessException ex){return false;}};
        Future<Boolean>a=pool.submit(task),b=pool.submit(task);ready.await(10,TimeUnit.SECONDS);go.countDown();int successes=(a.get()?1:0)+(b.get()?1:0);pool.shutdownNow();
        assertThat(successes).isEqualTo(1);var stock=inventories.lock(productId);assertThat(stock.getReservedQty()).isEqualTo(1);assertThat(stock.availableQty()).isZero();
    }

    private long newProduct(String label){String suffix=UUID.randomUUID().toString().substring(0,8);var p=new ProductMapper.ProductRow();p.code="T-"+label+"-"+suffix;p.barcode="99"+System.nanoTime();p.name="集成测试商品"+suffix;p.categoryId=1L;p.specification="1件";p.unit="件";p.purchasePrice=new BigDecimal("3.50");p.salePrice=new BigDecimal("5.90");p.status="ON_SALE";p.lowStockThreshold=1;products.insert(p);products.initInventory(p.id);return p.id;}
}
