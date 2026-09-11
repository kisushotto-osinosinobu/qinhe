package com.retailable.supermarket.purchase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public final class PurchaseDtos {
    private PurchaseDtos(){}
    public record PurchaseRequest(@NotNull @Positive Long supplierId,@Size(max=500)String remark,
                                  @NotEmpty List<@Valid PurchaseLine> items){}
    public record PurchaseLine(@NotNull @Positive Long productId,@Min(1)int quantity,
                               @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal unitPrice){}
}

