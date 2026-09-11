package com.retailable.supermarket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@MapperScan("com.retailable.supermarket")
public class SupermarketApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupermarketApplication.class, args);
    }
}
