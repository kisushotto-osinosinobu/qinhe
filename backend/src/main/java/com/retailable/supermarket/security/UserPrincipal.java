package com.retailable.supermarket.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UserPrincipal(Long id, String username, String password, String displayName, String role,
                            int tokenVersion, boolean enabled) implements UserDetails {
    public static UserPrincipal from(UserAccount user) {
        return new UserPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(), user.getDisplayName(),
                user.getRole(), user.getTokenVersion(), "ACTIVE".equals(user.getStatus()));
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
