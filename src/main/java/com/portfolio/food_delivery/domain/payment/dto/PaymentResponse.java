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
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Integer amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private String cardNumber; // 마스킹된 번호
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private String failureReason;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .cardNumber(payment.getCardNumber())
                .paidAt(payment.getPaidAt())
                .cancelledAt(payment.getCancelledAt())
                .cancelReason(payment.getCancelReason())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}