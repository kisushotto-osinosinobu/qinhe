package com.retailable.supermarket.auth;

import com.retailable.supermarket.common.ApiResponse;
import com.retailable.supermarket.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.retailable.supermarket.auth.AuthDtos.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/register")
    public ApiResponse<UserView> register(@Valid @RequestBody RegisterRequest request) { return ApiResponse.ok(service.register(request)); }

    @PostMapping("/login")
    public ApiResponse<AuthResult> login(@Valid @RequestBody LoginRequest request) { return ApiResponse.ok(service.login(request)); }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        service.logout((String) request.getAttribute("jwtJti"), principal.id());
        return ApiResponse.ok();
    }

    @GetMapping("/profile")
    public ApiResponse<UserView> profile(@AuthenticationPrincipal UserPrincipal principal) { return ApiResponse.ok(service.profile(principal.id())); }

    @PutMapping("/profile")
    public ApiResponse<UserView> updateProfile(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ProfileRequest request) {
        return ApiResponse.ok(service.updateProfile(principal.id(), request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody PasswordRequest request) {
        service.changePassword(principal.id(), request);
        return ApiResponse.ok();
    }
}
