package com.portfolio.food_delivery.config;

import com.portfolio.food_delivery.domain.payment.service.MockPaymentGatewayService;
import com.portfolio.food_delivery.domain.payment.service.PaymentGatewayService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public PaymentGatewayService paymentGatewayService() {
        return new MockPaymentGatewayService();
    }
}