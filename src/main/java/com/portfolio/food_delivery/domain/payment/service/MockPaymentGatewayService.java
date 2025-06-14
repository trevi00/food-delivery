// MockPaymentGatewayService.java - 개발/테스트용 Mock 구현체 (수정됨)
package com.portfolio.food_delivery.domain.payment.service;

import com.portfolio.food_delivery.domain.payment.dto.PaymentRequest;
import com.portfolio.food_delivery.domain.payment.entity.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MockPaymentGatewayService implements PaymentGatewayService {

    // 거래 정보를 메모리에 저장 (실제로는 PG사 서버에 저장됨)
    private final ConcurrentHashMap<String, PaymentGatewayResponse> transactions = new ConcurrentHashMap<>();

    @Override
    public PaymentGatewayResponse processPayment(PaymentRequest paymentRequest, Integer amount) {
        log.info("Mock PG: 결제 처리 시작 - 금액: {}원, 수단: {}", amount, paymentRequest.getPaymentMethod());

        // 테스트를 위한 시뮬레이션
        try {
            Thread.sleep(1000); // PG사 처리 시간 시뮬레이션
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 특정 카드번호로 실패 시뮬레이션
        if (paymentRequest.getCardNumber() != null && paymentRequest.getCardNumber().startsWith("9999")) {
            return new PaymentGatewayResponse(false, null, null, "카드 한도 초과");
        }

        // 성공 케이스
        String transactionId = "TXN_" + UUID.randomUUID().toString();
        String maskedCardNumber = maskCardNumber(paymentRequest.getCardNumber());

        PaymentGatewayResponse response = new PaymentGatewayResponse(
                true,
                transactionId,
                maskedCardNumber,
                null
        );

        transactions.put(transactionId, response);

        log.info("Mock PG: 결제 성공 - 거래ID: {}", transactionId);
        return response;
    }

    @Override
    public boolean cancelPayment(String transactionId, Integer amount, String reason) {
        log.info("Mock PG: 결제 취소 - 거래ID: {}, 금액: {}원, 사유: {}", transactionId, amount, reason);

        // 테스트 환경을 위한 개선: 특정 패턴의 거래 ID는 무조건 성공 처리
        if (transactionId != null && (transactionId.startsWith("TXN_CONFIRMED_") ||
                transactionId.startsWith("TXN_") ||
                transactionId.startsWith("TEST_"))) {
            log.info("Mock PG: 결제 취소 성공 - 거래ID: {} (테스트/확정 거래)", transactionId);
            // 메모리에서도 제거 (있다면)
            transactions.remove(transactionId);
            return true;
        }

        // 일반적인 케이스: 메모리에서 거래 정보 확인
        PaymentGatewayResponse transaction = transactions.get(transactionId);
        if (transaction == null) {
            log.error("Mock PG: 거래를 찾을 수 없음 - 거래ID: {}", transactionId);
            return false;
        }

        // 취소 성공
        transactions.remove(transactionId);
        log.info("Mock PG: 결제 취소 성공 - 거래ID: {}", transactionId);
        return true;
    }

    @Override
    public PaymentGatewayResponse getPaymentStatus(String transactionId) {
        // 테스트 거래 ID에 대해서는 기본 성공 응답 반환
        if (transactionId != null && (transactionId.startsWith("TXN_CONFIRMED_") ||
                transactionId.startsWith("TEST_"))) {
            return new PaymentGatewayResponse(
                    true,
                    transactionId,
                    "**** **** **** 1234",
                    null
            );
        }

        return transactions.get(transactionId);
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    // 테스트 지원을 위한 메서드들
    public void clearTransactions() {
        transactions.clear();
        log.info("Mock PG: 거래 내역 초기화");
    }

    public int getTransactionCount() {
        return transactions.size();
    }

    public boolean hasTransaction(String transactionId) {
        return transactions.containsKey(transactionId);
    }
}