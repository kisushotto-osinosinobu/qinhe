package com.retailable.supermarket.stats;

import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper {
    @Select("SELECT COALESCE(SUM(paid_amount-refund_amount),0) sales_amount,COUNT(*) order_count FROM sale_order WHERE status IN ('PAID','PARTIALLY_RETURNED','RETURNED') AND DATE(paid_at)=CURRENT_DATE") Map<String,Object> today();
    @Select("SELECT COUNT(*) FROM product") long productCount();
    @Select("SELECT COUNT(*) FROM inventory i JOIN product p ON p.id=i.product_id WHERE i.current_qty-i.reserved_qty<=p.low_stock_threshold") long lowStockCount();
    @Select("SELECT DATE(paid_at) sale_date,COALESCE(SUM(paid_amount-refund_amount),0) net_sales,COUNT(*) order_count FROM sale_order WHERE status IN ('PAID','PARTIALLY_RETURNED','RETURNED') AND DATE(paid_at) BETWEEN #{from} AND #{to} GROUP BY DATE(paid_at) ORDER BY sale_date") List<Map<String,Object>> trend(@Param("from")LocalDate from,@Param("to")LocalDate to);
    @Select("SELECT si.product_id,si.product_code,si.product_name,SUM(si.quantity-si.returned_qty) sold_qty,SUM((si.quantity-si.returned_qty)*si.unit_price) net_sales FROM sale_item si JOIN sale_order so ON so.id=si.sale_order_id WHERE so.status IN ('PAID','PARTIALLY_RETURNED','RETURNED') AND DATE(so.paid_at) BETWEEN #{from} AND #{to} GROUP BY si.product_id,si.product_code,si.product_name ORDER BY sold_qty DESC LIMIT #{limit}") List<Map<String,Object>> hot(@Param("from")LocalDate from,@Param("to")LocalDate to,@Param("limit")int limit);
    @Select("SELECT so.order_no,so.channel,so.status,so.total_amount,so.paid_amount,so.refund_amount,(so.paid_amount-so.refund_amount) net_amount,so.payment_method,so.paid_at,so.created_at,m.username member_username,c.username cashier_username FROM sale_order so LEFT JOIN sys_user m ON m.id=so.member_id LEFT JOIN sys_user c ON c.id=so.cashier_id WHERE DATE(so.created_at) BETWEEN #{from} AND #{to} ORDER BY so.id") List<Map<String,Object>> exportOrders(@Param("from")LocalDate from,@Param("to")LocalDate to);
}

