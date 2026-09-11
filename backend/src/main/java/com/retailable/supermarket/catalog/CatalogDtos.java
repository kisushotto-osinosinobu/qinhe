package com.retailable.supermarket.catalog;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public final class CatalogDtos {
    private CatalogDtos() {}

    public record CategoryRequest(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Pattern(regexp = "^[A-Z0-9_-]{2,40}$", message = "仅支持2-40位大写字母、数字、下划线或连字符") String code,
            @Min(0) @Max(9999) int sortOrder,
            @Pattern(regexp = "ENABLED|DISABLED") String status) {}

    public record ProductRequest(
            @NotBlank @Size(max = 40) String code,
            @NotBlank @Pattern(regexp = "^[0-9A-Za-z-]{6,64}$", message = "条码格式不正确") String barcode,
            @NotBlank @Size(max = 120) String name,
            @NotNull @Positive Long categoryId,
            @Size(max = 120) String specification,
            @NotBlank @Size(max = 20) String unit,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal purchasePrice,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal salePrice,
            @Size(max = 255) String imageUrl,
            @Pattern(regexp = "ON_SALE|OFF_SALE") String status,
            @Min(0) @Max(1000000) int lowStockThreshold) {}
}

