package com.retailable.supermarket.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public final class OrderDtos {
    private OrderDtos(){}
    public record CreateOrderRequest(@NotBlank @Size(max=80)String idempotencyKey,
                                     @NotBlank @Pattern(regexp="WEB_POS|MINIAPP")String channel,
                                     @NotEmpty List<@Valid OrderLine> items,
                                     Boolean autoPay,@Pattern(regexp="CASH|CARD|MOBILE|STORE")String paymentMethod){}
    public record OrderLine(@NotNull @Positive Long productId,@Min(1)int quantity){}
    public record PayRequest(@NotBlank @Size(max=80)String idempotencyKey,
                             @NotBlank @Pattern(regexp="CASH|CARD|MOBILE|STORE")String paymentMethod){}
    public record ReturnRequest(@NotBlank @Size(max=80)String idempotencyKey,@NotBlank @Size(max=255)String reason,
                                @NotEmpty List<@Valid ReturnLine> items){}
    public record ReturnLine(@NotNull @Positive Long saleItemId,@Min(1)int quantity){}
}
