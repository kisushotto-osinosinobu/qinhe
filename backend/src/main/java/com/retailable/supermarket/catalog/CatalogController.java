package com.retailable.supermarket.catalog;

import com.retailable.supermarket.common.ApiResponse;
import com.retailable.supermarket.common.PageResult;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static com.retailable.supermarket.catalog.CatalogDtos.*;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final CatalogService service;
    public CatalogController(CatalogService service){this.service=service;}

    @GetMapping("/public/categories") public ApiResponse<List<Map<String,Object>>> publicCategories(){return ApiResponse.ok(service.publicCategories());}
    @GetMapping("/public/products") public ApiResponse<PageResult<Map<String,Object>>> publicProducts(@RequestParam(required=false) String keyword,@RequestParam(required=false) Long categoryId,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int pageSize){return ApiResponse.ok(service.products(keyword,categoryId,true,page,pageSize));}
    @GetMapping("/public/products/{id}") public ApiResponse<Map<String,Object>> publicProduct(@PathVariable Long id){return ApiResponse.ok(service.product(id,true));}

    @GetMapping("/categories") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ApiResponse<PageResult<Map<String,Object>>> categories(@RequestParam(required=false) String keyword,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int pageSize){return ApiResponse.ok(service.categories(keyword,page,pageSize));}
    @PostMapping("/categories") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ApiResponse<Map<String,Object>> createCategory(@Valid @RequestBody CategoryRequest r){return ApiResponse.ok(service.createCategory(r));}
    @PutMapping("/categories/{id}") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ApiResponse<Map<String,Object>> updateCategory(@PathVariable Long id,@Valid @RequestBody CategoryRequest r){return ApiResponse.ok(service.updateCategory(id,r));}
    @DeleteMapping("/categories/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id){service.deleteCategory(id);return ApiResponse.ok();}

    @GetMapping("/products") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER','CASHIER')")
    public ApiResponse<PageResult<Map<String,Object>>> products(@RequestParam(required=false) String keyword,@RequestParam(required=false) Long categoryId,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int pageSize){return ApiResponse.ok(service.products(keyword,categoryId,false,page,pageSize));}
    @GetMapping("/products/barcode/{barcode}") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER','CASHIER')")
    public ApiResponse<Map<String,Object>> barcode(@PathVariable String barcode){return ApiResponse.ok(service.barcode(barcode));}
    @GetMapping("/products/{id}") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER','CASHIER')")
    public ApiResponse<Map<String,Object>> product(@PathVariable Long id){return ApiResponse.ok(service.product(id,false));}
    @PostMapping("/products") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ApiResponse<Map<String,Object>> createProduct(@Valid @RequestBody ProductRequest r){return ApiResponse.ok(service.createProduct(r));}
    @PutMapping("/products/{id}") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ApiResponse<Map<String,Object>> updateProduct(@PathVariable Long id,@Valid @RequestBody ProductRequest r){return ApiResponse.ok(service.updateProduct(id,r));}
    @PostMapping("/products/images") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ApiResponse<Map<String,String>> upload(@RequestPart("file") MultipartFile file){return ApiResponse.ok(service.upload(file));}
}

