package com.portfolio.food_delivery.domain.payment.dto;

import com.portfolio.food_delivery.domain.payment.entity.Payment;
import com.portfolio.food_delivery.domain.payment.entity.PaymentMethod;
import com.portfolio.food_delivery.domain.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "결제 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    @Schema(description = "결제 ID", example = "1")
    private Long id;

    @Schema(description = "주문 ID", example = "1")
    private Long orderId;

    @Schema(description = "결제 금액", example = "28000")
    private Integer amount;

    @Schema(description = "결제 수단", example = "CREDIT_CARD")
    private PaymentMethod method;

    @Schema(description = "결제 상태", example = "SUCCESS")
    private PaymentStatus status;

    @Schema(description = "거래 ID", example = "TXN_123456")
    private String transactionId;

    @Schema(description = "마스킹된 카드번호", example = "**** **** **** 1234")
    private String cardNumber;

    @Schema(description = "결제 완료일시", example = "2025-01-15T12:35:00")
    private LocalDateTime paidAt;

    @Schema(description = "결제 취소일시", example = "2025-01-15T15:00:00")
    private LocalDateTime cancelledAt;

    @Schema(description = "취소 사유", example = "고객 변심")
    private String cancelReason;

    @Schema(description = "실패 사유", example = "카드 한도 초과")
    private String failureReason;

    @Schema(description = "생성일시", example = "2025-01-15T12:30:00")
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