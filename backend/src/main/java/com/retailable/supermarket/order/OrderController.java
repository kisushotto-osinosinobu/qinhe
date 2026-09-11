package com.retailable.supermarket.order;

import com.retailable.supermarket.common.ApiResponse;
import com.retailable.supermarket.common.PageResult;
import com.retailable.supermarket.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.retailable.supermarket.order.OrderDtos.*;

@RestController @RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;public OrderController(OrderService service){this.service=service;}
    @GetMapping public ApiResponse<PageResult<Map<String,Object>>> page(@AuthenticationPrincipal UserPrincipal p,@RequestParam(required=false)String keyword,@RequestParam(required=false)String status,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="20")int pageSize){return ApiResponse.ok(service.page(p,keyword,status,page,pageSize));}
    @GetMapping("/{id}") public ApiResponse<Map<String,Object>> detail(@PathVariable Long id,@AuthenticationPrincipal UserPrincipal p){return ApiResponse.ok(service.detail(id,p));}
    @PostMapping public ApiResponse<Map<String,Object>> create(@Valid @RequestBody CreateOrderRequest r,@AuthenticationPrincipal UserPrincipal p){return ApiResponse.ok(service.create(r,p));}
    @PostMapping("/{id}/cancel") public ApiResponse<Map<String,Object>> cancel(@PathVariable Long id,@AuthenticationPrincipal UserPrincipal p){return ApiResponse.ok(service.cancel(id,p));}
    @PostMapping("/{id}/pay") @PreAuthorize("hasAnyRole('ADMIN','CASHIER')") public ApiResponse<Map<String,Object>> pay(@PathVariable Long id,@Valid @RequestBody PayRequest r,@AuthenticationPrincipal UserPrincipal p){return ApiResponse.ok(service.pay(id,r,p));}
    @PostMapping("/{id}/returns") @PreAuthorize("hasAnyRole('ADMIN','CASHIER')") public ApiResponse<Map<String,Object>> returnGoods(@PathVariable Long id,@Valid @RequestBody ReturnRequest r,@AuthenticationPrincipal UserPrincipal p){return ApiResponse.ok(service.returnGoods(id,r,p));}
}
