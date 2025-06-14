package com.portfolio.food_delivery.domain.payment.service;

import com.portfolio.food_delivery.domain.payment.dto.PaymentRequest;
import com.portfolio.food_delivery.domain.payment.service.PaymentGatewayService.PaymentGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Profile({"local", "test"}) // local과 test 프로파일에서만 활성화
public class MockPaymentGatewayService implements PaymentGatewayService {

    // 거래 정보를 메모리에 저장 (실제로는 PG사 서버에 저장됨)
    private final Map<String, PaymentGatewayResponse> transactions = new ConcurrentHashMap<>();

    @Override
    public PaymentGatewayResponse processPayment(PaymentRequest paymentRequest, Integer amount) {
        log.info("Mock PG: 결제 처리 시작 - 금액: {}원, 수단: {}", amount, paymentRequest.getPaymentMethod());

        // 테스트를 위한 시뮬레이션
        simulateProcessingTime();

        // 특정 카드번호로 실패 시뮬레이션
        if (shouldFailPayment(paymentRequest)) {
            log.error("Mock PG: 결제 실패 - 카드 한도 초과");
            return new PaymentGatewayResponse(false, null, null, "카드 한도 초과");
        }

        // 성공 케이스
        String transactionId = generateTransactionId();
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

        // null 체크
        if (transactionId == null) {
            log.error("Mock PG: 거래 ID가 null입니다");
            return false;
        }

        // 특정 패턴의 거래 ID는 무조건 성공 처리 (테스트용)
        if (isTestTransactionId(transactionId)) {
            log.info("Mock PG: 결제 취소 성공 - 거래ID: {} (테스트 거래)", transactionId);
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
        log.debug("Mock PG: 결제 상태 조회 - 거래ID: {}", transactionId);

        // 테스트 거래 ID에 대해서는 기본 성공 응답 반환
        if (transactionId != null && isTestTransactionId(transactionId)) {
            return new PaymentGatewayResponse(
                    true,
                    transactionId,
                    "**** **** **** 1234",
                    null
            );
        }

        PaymentGatewayResponse response = transactions.get(transactionId);
        if (response == null) {
            log.warn("Mock PG: 거래를 찾을 수 없음 - 거래ID: {}", transactionId);
            return new PaymentGatewayResponse(false, null, null, "거래를 찾을 수 없습니다");
        }

        return response;
    }

    // Helper methods
    private void simulateProcessingTime() {
        try {
            Thread.sleep(100); // 100ms 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Mock PG: 처리 시뮬레이션 중 인터럽트 발생");
        }
    }

    private boolean shouldFailPayment(PaymentRequest request) {
        return request.getCardNumber() != null && request.getCardNumber().startsWith("9999");
    }

    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** **** **** ****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    private boolean isTestTransactionId(String transactionId) {
        return transactionId.startsWith("TXN_CONFIRMED_") ||
                transactionId.startsWith("TEST_") ||
                transactionId.startsWith("TXN_");
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

    // 테스트용 거래 추가
    public void addTestTransaction(String transactionId, boolean success) {
        PaymentGatewayResponse response = new PaymentGatewayResponse(
                success,
                transactionId,
                "**** **** **** 1234",
                success ? null : "테스트 실패"
        );
        transactions.put(transactionId, response);
        log.info("Mock PG: 테스트 거래 추가 - ID: {}, 성공: {}", transactionId, success);
    }
}