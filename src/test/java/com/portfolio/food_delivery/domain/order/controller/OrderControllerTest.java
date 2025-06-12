package com.portfolio.food_delivery.domain.order.controller;

import com.portfolio.food_delivery.common.BaseIntegrationTest;
import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.repository.MenuRepository;
import com.portfolio.food_delivery.domain.order.dto.OrderCreateRequest;
import com.portfolio.food_delivery.domain.order.dto.OrderItemRequest;
import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.order.entity.OrderItem;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import com.portfolio.food_delivery.domain.order.repository.OrderRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String customerToken;
    private String ownerToken;
    private User customer;
    private User owner;
    private Restaurant restaurant;
    private Menu menu1;
    private Menu menu2;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        menuRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

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

        // 레스토랑 오너 생성
        owner = User.builder()
                .email("owner@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("사장님")
                .phoneNumber("010-3333-4444")
                .role(UserRole.RESTAURANT_OWNER)
                .build();
        owner = userRepository.save(owner);

        // 레스토랑 생성
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

        // 메뉴 생성
        menu1 = Menu.builder()
                .restaurant(restaurant)
                .name("양념치킨")
                .price(20000)
                .displayOrder(1)
                .build();
        menu1 = menuRepository.save(menu1);

        menu2 = Menu.builder()
                .restaurant(restaurant)
                .name("콜라")
                .price(2000)
                .displayOrder(2)
                .build();
        menu2 = menuRepository.save(menu2);

        // 토큰 발급
        customerToken = getAccessToken("customer@example.com", "password123!");
        ownerToken = getAccessToken("owner@example.com", "password123!");
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
    @DisplayName("주문 생성 성공")
    void createOrder_Success() throws Exception {
        // given
        OrderCreateRequest request = OrderCreateRequest.builder()
                .restaurantId(restaurant.getId())
                .orderItems(Arrays.asList(
                        OrderItemRequest.builder()
                                .menuId(menu1.getId())
                                .quantity(1)
                                .build(),
                        OrderItemRequest.builder()
                                .menuId(menu2.getId())
                                .quantity(2)
                                .build()
                ))
                .deliveryAddress(new Address("서울시", "강남구", "테헤란로", "789", "12347"))
                .phoneNumber("010-5555-6666")
                .request("문 앞에 놔주세요")
                .build();

        // when & then
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(24000)) // 20000 + 2000*2
                .andExpect(jsonPath("$.deliveryFee").value(3000))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.orderItems", hasSize(2)));
    }

    @Test
    @DisplayName("주문 생성 실패 - 최소 주문 금액 미달")
    void createOrder_MinimumOrderAmountFail() throws Exception {
        // given
        OrderCreateRequest request = OrderCreateRequest.builder()
                .restaurantId(restaurant.getId())
                .orderItems(Arrays.asList(
                        OrderItemRequest.builder()
                                .menuId(menu2.getId())  // 콜라만 주문
                                .quantity(1)
                                .build()
                ))
                .deliveryAddress(new Address("서울시", "강남구", "테헤란로", "789", "12347"))
                .phoneNumber("010-5555-6666")
                .build();

        // when & then
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));  // 메시지 대신 코드로 확인
    }

    @Test
    @DisplayName("주문 조회 성공 - 주문자")
    void getOrder_ByCustomer_Success() throws Exception {
        // given
        Order order = createOrder();

        // when & then
        mockMvc.perform(get("/api/orders/{orderId}", order.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.userId").value(customer.getId()))
                .andExpect(jsonPath("$.restaurantName").value("맛있는 치킨"));
    }

    @Test
    @DisplayName("주문 조회 성공 - 레스토랑 오너")
    void getOrder_ByOwner_Success() throws Exception {
        // given
        Order order = createOrder();

        // when & then
        mockMvc.perform(get("/api/orders/{orderId}", order.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()));
    }

    @Test
    @DisplayName("내 주문 목록 조회")
    void getMyOrders_Success() throws Exception {
        // given
        createOrder();
        createOrder();

        // when & then
        mockMvc.perform(get("/api/orders/my")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("주문 상태 변경 성공 - 레스토랑 오너")
    void updateOrderStatus_Success() throws Exception {
        // given
        Order order = createOrder();

        // when & then
        mockMvc.perform(patch("/api/orders/{orderId}/status", order.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("status", "PREPARING"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));
    }

    @Test
    @DisplayName("주문 상태 변경 실패 - 권한 없음")
    void updateOrderStatus_Unauthorized() throws Exception {
        // given
        Order order = createOrder();

        // when & then
        mockMvc.perform(patch("/api/orders/{orderId}/status", order.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .param("status", "PREPARING"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("R003"));  // 코드로 확인
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_Success() throws Exception {
        // given
        Order order = createOrder();

        // when & then
        mockMvc.perform(post("/api/orders/{orderId}/cancel", order.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("주문 취소 실패 - 이미 조리 시작")
    void cancelOrder_AlreadyPreparing_Fail() throws Exception {
        // given
        Order order = createOrder();
        order.updateStatus(OrderStatus.PREPARING);
        orderRepository.save(order);

        // when & then
        mockMvc.perform(post("/api/orders/{orderId}/cancel", order.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));  // 코드로 확인
    }

    private Order createOrder() {
        Order order = Order.builder()
                .user(customer)
                .restaurant(restaurant)
                .deliveryAddress(new Address("서울시", "강남구", "테헤란로", "789", "12347"))
                .phoneNumber("010-5555-6666")
                .totalAmount(20000)
                .deliveryFee(3000)
                .status(OrderStatus.PENDING)
                .orderedAt(java.time.LocalDateTime.now())
                .build();

        // OrderItem 추가
        OrderItem orderItem = OrderItem.builder()
                .menu(menu1)
                .quantity(1)
                .price(menu1.getPrice())
                .build();

        order.addOrderItem(orderItem);

        return orderRepository.save(order);
    }
}