package com.retailable.supermarket.supplier;

import com.retailable.supermarket.common.ApiResponse;
import com.retailable.supermarket.common.PageResult;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.retailable.supermarket.supplier.SupplierDtos.SupplierRequest;

@RestController @RequestMapping("/api/suppliers") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
public class SupplierController {
    private final SupplierService service; public SupplierController(SupplierService service){this.service=service;}
    @GetMapping public ApiResponse<PageResult<Map<String,Object>>> page(@RequestParam(required=false)String keyword,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="20")int pageSize){return ApiResponse.ok(service.page(keyword,page,pageSize));}
    @GetMapping("/enabled") public ApiResponse<List<Map<String,Object>>> enabled(){return ApiResponse.ok(service.enabled());}
    @PostMapping public ApiResponse<Map<String,Object>> create(@Valid @RequestBody SupplierRequest r){return ApiResponse.ok(service.create(r));}
    @PutMapping("/{id}") public ApiResponse<Map<String,Object>> update(@PathVariable Long id,@Valid @RequestBody SupplierRequest r){return ApiResponse.ok(service.update(id,r));}
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiResponse<Void> delete(@PathVariable Long id){service.delete(id);return ApiResponse.ok();}
}
