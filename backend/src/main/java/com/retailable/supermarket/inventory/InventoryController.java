package com.retailable.supermarket.inventory;

import com.retailable.supermarket.common.ApiResponse;
import com.retailable.supermarket.common.PageResult;
import com.retailable.supermarket.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @RequestMapping("/api/inventory") @Validated
@PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER','CASHIER')")
public class InventoryController {
    private final InventoryService service; public InventoryController(InventoryService service){this.service=service;}
    @GetMapping public ApiResponse<PageResult<Map<String,Object>>> page(@RequestParam(required=false)String keyword,@RequestParam(defaultValue="false")boolean lowOnly,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="20")int pageSize){return ApiResponse.ok(service.page(keyword,lowOnly,page,pageSize));}
    @GetMapping("/movements") public ApiResponse<PageResult<Map<String,Object>>> movements(@RequestParam(required=false)Long productId,@RequestParam(required=false)String businessType,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="20")int pageSize){return ApiResponse.ok(service.movements(productId,businessType,page,pageSize));}
    @PostMapping("/{productId}/adjust") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ApiResponse<InventoryRow> adjust(@PathVariable Long productId,@Valid @RequestBody AdjustRequest r,@AuthenticationPrincipal UserPrincipal p){return ApiResponse.ok(service.adjust(productId,r.targetQty(),p.id(),r.reason()));}
    @PostMapping("/{productId}/movement") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ApiResponse<InventoryRow> movement(@PathVariable Long productId,@Valid @RequestBody MovementRequest r,@AuthenticationPrincipal UserPrincipal p){return ApiResponse.ok(service.manualMovement(productId,r.quantity(),"IN".equals(r.direction()),p.id(),r.reason()));}
    public record AdjustRequest(@Min(0)int targetQty,@NotBlank String reason){}
    public record MovementRequest(@Min(1)int quantity,@jakarta.validation.constraints.Pattern(regexp="IN|OUT")String direction,@NotBlank String reason){}
}
