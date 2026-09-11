package com.retailable.supermarket.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class SupplierDtos {
    private SupplierDtos() {}
    public record SupplierRequest(
            @NotBlank @Size(max=40) String code,
            @NotBlank @Size(max=120) String name,
            @Size(max=80) String contactName,
            @Size(max=30) String phone,
            @Email @Size(max=120) String email,
            @Size(max=255) String address,
            @Pattern(regexp="ENABLED|DISABLED") String status,
            @Size(max=500) String remark) {}
}

