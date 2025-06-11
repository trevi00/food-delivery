package com.portfolio.food_delivery.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public RequestPostProcessor testUser() {
        return SecurityMockMvcRequestPostProcessors.user("test@example.com")
                .roles("CUSTOMER");
    }
}