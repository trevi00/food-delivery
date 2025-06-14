package com.portfolio.food_delivery.domain.payment.service;

import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import com.portfolio.food_delivery.domain.order.repository.OrderRepository;
import com.portfolio.food_delivery.domain.payment.dto.*;
import com.portfolio.food_delivery.domain.payment.entity.Payment;
import com.portfolio.food_delivery.domain.payment.entity.PaymentMethod;
import com.portfolio.food_delivery.domain.payment.entity.PaymentStatus;
import com.portfolio.food_delivery.domain.payment.exception.*;
import com.portfolio.food_delivery.domain.payment.repository.PaymentRepository;
import com.portfolio.food_delivery.domain.payment.service.PaymentGatewayService.PaymentGatewayResponse;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 처리 성공")
    void processPayment_Success() {
        // given
        Long orderId = 1L;
        Order order = createOrder(orderId, OrderStatus.PENDING);

        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .cardNumber("1234567812345678")
                .cardExpiry("12/25")
                .cardCvc("123")
                .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderId(orderId)).willReturn(false);
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            return Payment.builder()
                    .id(1L)
                    .order(payment.getOrder())
                    .amount(payment.getAmount())
                    .method(payment.getMethod())
                    .status(payment.getStatus())
                    .build();
        });

        PaymentGatewayResponse pgResponse = new PaymentGatewayResponse(
                true, "TXN_123456", "**** **** **** 5678", null);
        given(paymentGatewayService.processPayment(any(), anyInt())).willReturn(pgResponse);

        // when
        PaymentResponse response = paymentService.processPayment(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getTransactionId()).isEqualTo("TXN_123456");
        assertThat(response.getCardNumber()).isEqualTo("**** **** **** 5678");

        verify(orderRepository).findById(orderId);
        verify(paymentRepository).existsByOrderId(orderId);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentGatewayService).processPayment(any(), anyInt());
    }

    @Test
    @DisplayName("결제 처리 실패 - PG사 거절")
    void processPayment_PGRejected() {
        // given
        Long orderId = 1L;
        Order order = createOrder(orderId, OrderStatus.PENDING);

        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .cardNumber("9999567812345678")  // 실패 케이스
                .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderId(orderId)).willReturn(false);
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

        PaymentGatewayResponse pgResponse = new PaymentGatewayResponse(
                false, null, null, "카드 한도 초과");
        given(paymentGatewayService.processPayment(any(), anyInt())).willReturn(pgResponse);

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(PaymentFailedException.class)
                .hasMessageContaining("카드 한도 초과");
    }

    @Test
    @DisplayName("결제 처리 실패 - 중복 결제")
    void processPayment_AlreadyProcessed() {
        // given
        Long orderId = 1L;
        Order order = createOrder(orderId, OrderStatus.PENDING);

        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderId(orderId)).willReturn(true);  // 이미 결제 존재

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(PaymentAlreadyProcessedException.class)
                .hasMessage("이미 결제가 완료된 주문입니다.");

        verify(paymentRepository, never()).save(any());
        verify(paymentGatewayService, never()).processPayment(any(), anyInt());
    }

    @Test
    @DisplayName("결제 취소 성공")
    void cancelPayment_Success() {
        // given
        Long paymentId = 1L;
        Order order = createOrder(1L, OrderStatus.CONFIRMED);
        Payment payment = createSuccessPayment(paymentId, order);

        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
        given(paymentGatewayService.cancelPayment(anyString(), anyInt(), anyString())).willReturn(true);

        // when
        PaymentResponse response = paymentService.cancelPayment(paymentId, "고객 요청");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(response.getCancelReason()).isEqualTo("고객 요청");

        verify(paymentRepository).findById(paymentId);
        verify(paymentGatewayService).cancelPayment("TXN_123456", 23000, "고객 요청");
    }

    @Test
    @DisplayName("결제 취소 실패 - 취소 불가능한 상태")
    void cancelPayment_InvalidStatus() {
        // given
        Long paymentId = 1L;
        Order order = createOrder(1L, OrderStatus.PENDING);
        Payment payment = Payment.builder()
                .id(paymentId)
                .order(order)
                .amount(23000)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)  // 아직 결제 안됨
                .build();

        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, "고객 요청"))
                .isInstanceOf(InvalidPaymentAmountException.class)
                .hasMessageContaining("취소할 수 없는 결제입니다");

        verify(paymentGatewayService, never()).cancelPayment(anyString(), anyInt(), anyString());
    }

    @Test
    @DisplayName("결제 내역 조회")
    void getPaymentHistory_Success() {
        // given
        Long userId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Order order1 = createOrderWithUser(1L, user, restaurant);
        Order order2 = createOrderWithUser(2L, user, restaurant);

        Payment payment1 = createSuccessPayment(1L, order1);
        Payment payment2 = createSuccessPayment(2L, order2);

        Page<Payment> paymentPage = new PageImpl<>(Arrays.asList(payment1, payment2), pageable, 2);

        given(paymentRepository.findByUserId(userId, pageable)).willReturn(paymentPage);

        // when
        Page<PaymentHistoryResponse> response = paymentService.getPaymentHistory(userId, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);

        verify(paymentRepository).findByUserId(userId, pageable);
    }

    // Helper methods
    private Order createOrder(Long id, OrderStatus status) {
        User user = createUser(1L);
        Restaurant restaurant = createRestaurant();

        return Order.builder()
                .id(id)
                .user(user)
                .restaurant(restaurant)
                .totalAmount(20000)
                .deliveryFee(3000)
                .status(status)
                .orderedAt(LocalDateTime.now())
                .build();
    }

    private Order createOrderWithUser(Long id, User user, Restaurant restaurant) {
        return Order.builder()
                .id(id)
                .user(user)
                .restaurant(restaurant)
                .totalAmount(20000)
                .deliveryFee(3000)
                .status(OrderStatus.CONFIRMED)
                .orderedAt(LocalDateTime.now())
                .build();
    }

    private Payment createSuccessPayment(Long id, Order order) {
        return Payment.builder()
                .id(id)
                .order(order)
                .amount(23000)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.SUCCESS)
                .transactionId("TXN_123456")
                .cardNumber("**** **** **** 5678")
                .paidAt(LocalDateTime.now())
                .build();
    }

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .email("user@example.com")
                .name("사용자")
                .build();
    }

    private Restaurant createRestaurant() {
        return Restaurant.builder()
                .id(1L)
                .name("맛있는 치킨")
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();
    }
}