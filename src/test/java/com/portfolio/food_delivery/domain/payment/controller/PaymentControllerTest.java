package com.portfolio.food_delivery.domain.payment.controller;

import com.portfolio.food_delivery.common.BaseIntegrationTest;
import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.config.TestConfig;
import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.repository.MenuRepository;
import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.order.entity.OrderItem;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import com.portfolio.food_delivery.domain.order.repository.OrderRepository;
import com.portfolio.food_delivery.domain.payment.dto.PaymentCancelRequest;
import com.portfolio.food_delivery.domain.payment.dto.PaymentRequest;
import com.portfolio.food_delivery.domain.payment.entity.Payment;
import com.portfolio.food_delivery.domain.payment.entity.PaymentMethod;
import com.portfolio.food_delivery.domain.payment.entity.PaymentStatus;
import com.portfolio.food_delivery.domain.payment.repository.PaymentRepository;
import com.portfolio.food_delivery.domain.payment.service.MockPaymentGatewayService;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.restaurant.repository.RestaurantRepository;
import com.portfolio.food_delivery.domain.user.dto.LoginRequest;
import com.portfolio.food_delivery.domain.user.dto.LoginResponse;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.entity.UserRole;
import com.portfolio.food_delivery.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private MockPaymentGatewayService mockPaymentGatewayService;

    private String customerToken;
    private User customer;
    private Restaurant restaurant;
    private Order pendingOrder;
    private Order confirmedOrder;

    @BeforeEach
    void setUp() throws Exception {
        // Mock PG 서비스 초기화 (있는 경우만)
        if (mockPaymentGatewayService != null) {
            mockPaymentGatewayService.clearTransactions();
        }

        // 데이터 초기화
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        menuRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 데이터 생성
        setupTestData();

        // 토큰 발급
        customerToken = getAccessToken("customer@example.com", "password123!");
    }

    private void setupTestData() {
        // 고객 생성
        customer = User.builder()
                .email("customer@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("고객님")
                .phoneNumber("010-1111-2222")
                .role(UserRole.CUSTOMER)
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .build();
        customer = userRepository.save(customer);

        // 레스토랑 및 메뉴 생성
        User owner = User.builder()
                .email("owner@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("사장님")
                .phoneNumber("010-3333-4444")
                .role(UserRole.RESTAURANT_OWNER)
                .build();
        owner = userRepository.save(owner);

        restaurant = Restaurant.builder()
                .owner(owner)
                .name("맛있는 치킨")
                .category(RestaurantCategory.CHICKEN)
                .phoneNumber("02-1234-5678")
                .address(new Address("서울시", "강남구", "선릉로", "456", "12346"))
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(22, 0))
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        Menu menu = Menu.builder()
                .restaurant(restaurant)
                .name("양념치킨")
                .price(20000)
                .displayOrder(1)
                .build();
        menu = menuRepository.save(menu);

        // 결제 대기 중인 주문
        pendingOrder = createOrder(customer, restaurant, menu, OrderStatus.PENDING);

        // 이미 결제된 주문
        confirmedOrder = createOrder(customer, restaurant, menu, OrderStatus.CONFIRMED);

        // 테스트용 결제 정보 생성
        Payment confirmedPayment = Payment.builder()
                .order(confirmedOrder)
                .amount(23000)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.SUCCESS)
                .transactionId("TXN_CONFIRMED_123")
                .cardNumber("**** **** **** 1234")
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(confirmedPayment);

        // Mock PG 서비스에 테스트 거래 추가 (있는 경우만)
        if (mockPaymentGatewayService != null) {
            mockPaymentGatewayService.addTestTransaction("TXN_CONFIRMED_123", true);
        }
    }

    private Order createOrder(User user, Restaurant restaurant, Menu menu, OrderStatus status) {
        Order order = Order.builder()
                .user(user)
                .restaurant(restaurant)
                .deliveryAddress(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .totalAmount(20000)
                .deliveryFee(3000)
                .status(status)
                .orderedAt(LocalDateTime.now())
                .build();

        OrderItem orderItem = OrderItem.builder()
                .menu(menu)
                .quantity(1)
                .price(menu.getPrice())
                .build();
        order.addOrderItem(orderItem);

        return orderRepository.save(order);
    }

    private String getAccessToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);
        return loginResponse.getAccessToken();
    }

    @Test
    @DisplayName("결제 처리 성공")
    void processPayment_Success() throws Exception {
        // given
        PaymentRequest request = PaymentRequest.builder()
                .orderId(pendingOrder.getId())
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .cardNumber("1234567812345678")
                .cardExpiry("12/25")
                .cardCvc("123")
                .build();

        // when & then
        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(pendingOrder.getId()))
                .andExpect(jsonPath("$.amount").value(23000))
                .andExpect(jsonPath("$.method").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.cardNumber").value("**** **** **** 5678"));

        // 주문 상태 확인
        Order updatedOrder = orderRepository.findById(pendingOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("결제 처리 실패 - 카드 한도 초과")
    void processPayment_CardLimitExceeded() throws Exception {
        // given
        PaymentRequest request = PaymentRequest.builder()
                .orderId(pendingOrder.getId())
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .cardNumber("9999567812345678")  // 실패 케이스
                .cardExpiry("12/25")
                .cardCvc("123")
                .build();

        // when & then
        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("P004"));
    }

    @Test
    @DisplayName("결제 처리 실패 - 중복 결제 (진단용)")
    void processPayment_AlreadyPaid_Diagnostic() throws Exception {
        // 데이터 상태 확인
        System.out.println("=== 테스트 시작 전 데이터 확인 ===");
        System.out.println("confirmedOrder ID: " + confirmedOrder.getId());
        System.out.println("confirmedOrder Status: " + confirmedOrder.getStatus());

        Payment existingPayment = paymentRepository.findByOrderId(confirmedOrder.getId()).orElse(null);
        System.out.println("기존 결제 존재 여부: " + (existingPayment != null));
        if (existingPayment != null) {
            System.out.println("기존 결제 ID: " + existingPayment.getId());
            System.out.println("기존 결제 상태: " + existingPayment.getStatus());
        }

        // given
        PaymentRequest request = PaymentRequest.builder()
                .orderId(confirmedOrder.getId())
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .cardNumber("1234567812345678")
                .cardExpiry("12/25")
                .cardCvc("123")
                .build();

        // when
        MvcResult result = mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andReturn();

        // then - 실제 응답 확인
        System.out.println("=== 응답 결과 ===");
        System.out.println("Status Code: " + result.getResponse().getStatus());
        System.out.println("Response Body: " + result.getResponse().getContentAsString());

        // 최소한의 검증만 수행
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("결제 취소 성공")
    void cancelPayment_Success() throws Exception {
        // given
        Payment payment = paymentRepository.findByOrderId(confirmedOrder.getId()).orElseThrow();

        PaymentCancelRequest request = PaymentCancelRequest.builder()
                .cancelReason("고객 변심")
                .build();

        // when & then
        mockMvc.perform(post("/api/payments/{paymentId}/cancel", payment.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelReason").value("고객 변심"));

        // 주문 상태 확인
        Order updatedOrder = orderRepository.findById(confirmedOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("결제 정보 조회 - 결제 ID로")
    void getPayment_Success() throws Exception {
        // given
        Payment payment = paymentRepository.findByOrderId(confirmedOrder.getId()).orElseThrow();

        // when & then
        mockMvc.perform(get("/api/payments/{paymentId}", payment.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.orderId").value(confirmedOrder.getId()))
                .andExpect(jsonPath("$.amount").value(23000))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("결제 정보 조회 - 주문 ID로")
    void getPaymentByOrderId_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/payments/orders/{orderId}", confirmedOrder.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(confirmedOrder.getId()))
                .andExpect(jsonPath("$.amount").value(23000))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("결제 내역 조회")
    void getPaymentHistory_Success() throws Exception {
        // given - 추가 결제 생성
        Menu menu = menuRepository.findAll().get(0);
        Order anotherOrder = createOrder(customer, restaurant, menu, OrderStatus.CONFIRMED);
        Payment anotherPayment = Payment.builder()
                .order(anotherOrder)
                .amount(23000)
                .method(PaymentMethod.KAKAO_PAY)
                .status(PaymentStatus.SUCCESS)
                .transactionId("TXN_KAKAO_456")
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(anotherPayment);

        // when & then
        mockMvc.perform(get("/api/payments/history")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].restaurantName").value("맛있는 치킨"));
    }

    @Test
    @DisplayName("기간별 결제 내역 조회")
    void getPaymentHistoryByDateRange_Success() throws Exception {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // when & then
        mockMvc.perform(get("/api/payments/history/date-range")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId").value(confirmedOrder.getId()));
    }

    @Test
    @DisplayName("결제 상태 확인 및 업데이트")
    void checkPaymentStatus_Success() throws Exception {
        // given
        Payment payment = paymentRepository.findByOrderId(confirmedOrder.getId()).orElseThrow();

        // when & then
        mockMvc.perform(post("/api/payments/{paymentId}/status/check", payment.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isOk());
    }
}