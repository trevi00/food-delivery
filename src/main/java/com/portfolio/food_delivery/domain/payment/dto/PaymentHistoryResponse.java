package com.portfolio.food_delivery.domain.payment.dto;

import com.portfolio.food_delivery.domain.payment.entity.Payment;
import com.portfolio.food_delivery.domain.payment.entity.PaymentMethod;
import com.portfolio.food_delivery.domain.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistoryResponse {
    private Long paymentId;
    private Long orderId;
    private String restaurantName;
    private Integer amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime paidAt;

    public static PaymentHistoryResponse from(Payment payment) {
        return PaymentHistoryResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .restaurantName(payment.getOrder().getRestaurant().getName())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }
}