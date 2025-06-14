package com.portfolio.food_delivery.domain.payment.service;

import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import com.portfolio.food_delivery.domain.order.exception.OrderNotFoundException;
import com.portfolio.food_delivery.domain.order.repository.OrderRepository;
import com.portfolio.food_delivery.domain.payment.dto.*;
import com.portfolio.food_delivery.domain.payment.entity.Payment;
import com.portfolio.food_delivery.domain.payment.entity.PaymentStatus;
import com.portfolio.food_delivery.domain.payment.exception.*;
import com.portfolio.food_delivery.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayService paymentGatewayService;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // 1. 주문 조회 및 검증
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        // 2. 주문 상태 확인
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidPaymentAmountException("결제 대기 중인 주문만 결제할 수 있습니다.");
        }

        // 3. 중복 결제 확인
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new PaymentAlreadyProcessedException("이미 결제가 완료된 주문입니다.");
        }

        // 4. 결제 정보 생성
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount() + order.getDeliveryFee())
                .method(request.getPaymentMethod())
                .build();

        payment = paymentRepository.save(payment);

        try {
            // 5. 결제 처리 시작
            payment.startProcessing();

            // 6. PG사 결제 요청
            PaymentGatewayService.PaymentGatewayResponse pgResponse =
                    paymentGatewayService.processPayment(request, payment.getAmount());

            if (pgResponse.success()) {
                // 7. 결제 성공 처리
                payment.completePayment(pgResponse.transactionId(), pgResponse.maskedCardNumber());

                // 8. 주문 상태 업데이트
                order.updateStatus(OrderStatus.CONFIRMED);

                log.info("결제 성공 - 주문ID: {}, 결제ID: {}, 금액: {}원",
                        order.getId(), payment.getId(), payment.getAmount());
            } else {
                // 9. 결제 실패 처리
                payment.failPayment(pgResponse.failureReason());

                log.error("결제 실패 - 주문ID: {}, 사유: {}",
                        order.getId(), pgResponse.failureReason());

                throw new PaymentFailedException("결제 처리에 실패했습니다: " + pgResponse.failureReason());
            }

            return PaymentResponse.from(payment);

        } catch (Exception e) {
            // 10. 예외 발생 시 결제 실패 처리
            if (payment.getStatus() == PaymentStatus.PROCESSING) {
                payment.failPayment(e.getMessage());
            }

            if (e instanceof PaymentFailedException) {
                throw e;
            }

            throw new PaymentFailedException("결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponse cancelPayment(Long paymentId, String cancelReason) {
        // 1. 결제 정보 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        // 2. 취소 가능 여부 확인
        if (!payment.isCancellable()) {
            throw new InvalidPaymentAmountException("취소할 수 없는 결제입니다. 현재 상태: " + payment.getStatus());
        }

        // 3. PG사 취소 요청
        boolean cancelSuccess = paymentGatewayService.cancelPayment(
                payment.getTransactionId(), payment.getAmount(), cancelReason);

        if (!cancelSuccess) {
            throw new PaymentFailedException("결제 취소에 실패했습니다.");
        }

        // 4. 결제 취소 처리
        payment.cancelPayment(cancelReason);

        // 5. 주문 상태 업데이트
        Order order = payment.getOrder();
        order.cancel(cancelReason);

        log.info("결제 취소 완료 - 결제ID: {}, 주문ID: {}, 사유: {}",
                payment.getId(), order.getId(), cancelReason);

        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("해당 주문의 결제 정보를 찾을 수 없습니다."));

        return PaymentResponse.from(payment);
    }

    public Page<PaymentHistoryResponse> getPaymentHistory(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);

        return payments.map(PaymentHistoryResponse::from);
    }

    public List<PaymentHistoryResponse> getPaymentHistoryByDateRange(
            Long userId, LocalDateTime startDate, LocalDateTime endDate) {

        List<Payment> payments = paymentRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        return payments.stream()
                .map(PaymentHistoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void checkAndUpdatePaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        // PROCESSING 상태인 경우 PG사에 상태 확인
        if (payment.getStatus() == PaymentStatus.PROCESSING) {
            PaymentGatewayService.PaymentGatewayResponse status =
                    paymentGatewayService.getPaymentStatus(payment.getTransactionId());

            if (status != null && status.success()) {
                payment.completePayment(status.transactionId(), status.maskedCardNumber());
                payment.getOrder().updateStatus(OrderStatus.CONFIRMED);
            }
        }
    }
}