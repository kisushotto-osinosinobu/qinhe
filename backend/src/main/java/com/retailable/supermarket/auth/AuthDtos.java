package com.retailable.supermarket.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record RegisterRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_]{4,40}$", message = "仅支持4-40位字母、数字和下划线") String username,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 80) String displayName,
            @Size(max = 30) String phone,
            @Email @Size(max = 120) String email) {}
    public record ProfileRequest(@NotBlank @Size(max = 80) String displayName, @Size(max = 30) String phone,
                                 @Email @Size(max = 120) String email, @Size(max = 255) String avatarUrl) {}
    public record PasswordRequest(@NotBlank String oldPassword, @NotBlank @Size(min = 8, max = 72) String newPassword) {}
    public record AuthResult(String token, String expiresAt, UserView user) {}
    public record UserView(Long id, String username, String displayName, String phone, String email, String avatarUrl, String role) {}
}

