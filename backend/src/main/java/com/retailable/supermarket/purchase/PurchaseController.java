package com.retailable.supermarket.purchase;

import com.retailable.supermarket.common.ApiResponse;
import com.retailable.supermarket.common.PageResult;
import com.retailable.supermarket.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.retailable.supermarket.purchase.PurchaseDtos.PurchaseRequest;

@RestController @RequestMapping("/api/purchases") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
public class PurchaseController {
    private final PurchaseService service;public PurchaseController(PurchaseService service){this.service=service;}
    @GetMapping public ApiResponse<PageResult<Map<String,Object>>> page(@RequestParam(required=false)String keyword,@RequestParam(required=false)String status,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="20")int pageSize){return ApiResponse.ok(service.page(keyword,status,page,pageSize));}
    @GetMapping("/{id}") public ApiResponse<Map<String,Object>> detail(@PathVariable Long id){return ApiResponse.ok(service.detail(id));}
    @PostMapping public ApiResponse<Map<String,Object>> create(@Valid @RequestBody PurchaseRequest r,@AuthenticationPrincipal UserPrincipal p){return ApiResponse.ok(service.create(r,p.id()));}
    @PostMapping("/{id}/confirm") public ApiResponse<Map<String,Object>> confirm(@PathVariable Long id,@AuthenticationPrincipal UserPrincipal p){return ApiResponse.ok(service.confirm(id,p.id()));}
    @PostMapping("/{id}/cancel") public ApiResponse<Map<String,Object>> cancel(@PathVariable Long id){return ApiResponse.ok(service.cancel(id));}
}
