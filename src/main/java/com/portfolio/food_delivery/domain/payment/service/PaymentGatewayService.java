package com.portfolio.food_delivery.domain.payment.service;

import com.portfolio.food_delivery.domain.payment.dto.PaymentRequest;
import com.portfolio.food_delivery.domain.payment.entity.PaymentMethod;

public interface PaymentGatewayService {

    /**
     * PG사에 결제 요청
     * @param paymentRequest 결제 요청 정보
     * @param amount 결제 금액
     * @return PG사 거래 ID
     */
    PaymentGatewayResponse processPayment(PaymentRequest paymentRequest, Integer amount);

    /**
     * PG사에 결제 취소 요청
     * @param transactionId PG사 거래 ID
     * @param amount 취소 금액
     * @param reason 취소 사유
     * @return 취소 성공 여부
     */
    boolean cancelPayment(String transactionId, Integer amount, String reason);

    /**
     * 결제 상태 조회
     * @param transactionId PG사 거래 ID
     * @return 결제 상태
     */
    PaymentGatewayResponse getPaymentStatus(String transactionId);

    // PG사 응답 DTO
    record PaymentGatewayResponse(
            boolean success,
            String transactionId,
            String maskedCardNumber,
            String failureReason
    ) {}
}