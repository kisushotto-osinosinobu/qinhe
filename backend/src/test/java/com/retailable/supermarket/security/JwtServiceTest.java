package com.retailable.supermarket.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    @Test
    void issuedTokenContainsIdentityAndVersion() {
        JwtService service = new JwtService("0123456789abcdef0123456789abcdef", 30);
        var token = service.issue(new UserPrincipal(9L, "tester", "hash", "测试员", "CASHIER", 3, true));
        var claims = service.parse(token.value());
        assertThat(claims.getSubject()).isEqualTo("tester");
        assertThat(claims.get("uid", Long.class)).isEqualTo(9L);
        assertThat(claims.get("ver", Integer.class)).isEqualTo(3);
        assertThat(claims.getId()).isEqualTo(token.jti());
    }
}

