package com.portfolio.food_delivery.domain.cart.controller;

import com.portfolio.food_delivery.common.BaseIntegrationTest;
import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.cart.dto.CartItemRequest;
import com.portfolio.food_delivery.domain.cart.dto.CartUpdateRequest;
import com.portfolio.food_delivery.domain.cart.entity.Cart;
import com.portfolio.food_delivery.domain.cart.repository.CartRepository;
import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.entity.MenuStatus;
import com.portfolio.food_delivery.domain.menu.repository.MenuRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String customerToken;
    private User customer;
    private Restaurant restaurant;
    private Menu menu1;
    private Menu menu2;

    @BeforeEach
    void setUp() throws Exception {
        cartRepository.deleteAll();
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
        User owner = User.builder()
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
                .status(MenuStatus.AVAILABLE)
                .displayOrder(1)
                .build();
        menu1 = menuRepository.save(menu1);

        menu2 = Menu.builder()
                .restaurant(restaurant)
                .name("콜라")
                .price(2000)
                .status(MenuStatus.AVAILABLE)
                .displayOrder(2)
                .build();
        menu2 = menuRepository.save(menu2);

        // 토큰 발급
        customerToken = getAccessToken("customer@example.com", "password123!");
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
    @DisplayName("장바구니에 메뉴 추가 성공")
    void addToCart_Success() throws Exception {
        // given
        CartItemRequest request = CartItemRequest.builder()
                .menuId(menu1.getId())
                .quantity(2)
                .build();

        // when & then
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantName").value("맛있는 치킨"))
                .andExpect(jsonPath("$.totalAmount").value(40000))
                .andExpect(jsonPath("$.totalQuantity").value(2))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.canOrder").value(true));
    }

    @Test
    @DisplayName("장바구니에 같은 메뉴 추가 시 수량 증가")
    void addToCart_SameMenu_IncreaseQuantity() throws Exception {
        // given
        CartItemRequest request1 = CartItemRequest.builder()
                .menuId(menu1.getId())
                .quantity(1)
                .build();

        // 첫 번째 추가
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // 두 번째 추가
        CartItemRequest request2 = CartItemRequest.builder()
                .menuId(menu1.getId())
                .quantity(2)
                .build();

        // when & then
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.totalAmount").value(60000));
    }

    @Test
    @DisplayName("장바구니 조회 성공")
    void getCart_Success() throws Exception {
        // given
        Cart cart = Cart.builder()
                .user(customer)
                .build();
        cart = cartRepository.save(cart);

        // when & then
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(0))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    @DisplayName("장바구니 아이템 수량 변경 성공")
    void updateCartItem_Success() throws Exception {
        // given
        // 먼저 장바구니에 아이템 추가
        CartItemRequest addRequest = CartItemRequest.builder()
                .menuId(menu1.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated());

        // 수량 변경 요청
        CartUpdateRequest updateRequest = CartUpdateRequest.builder()
                .quantity(5)
                .build();

        // when & then
        mockMvc.perform(put("/api/cart/items/{menuId}", menu1.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.totalAmount").value(100000));
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 성공")
    void removeFromCart_Success() throws Exception {
        // given
        // 장바구니에 두 개의 아이템 추가
        CartItemRequest request1 = CartItemRequest.builder()
                .menuId(menu1.getId())
                .quantity(1)
                .build();

        CartItemRequest request2 = CartItemRequest.builder()
                .menuId(menu2.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(delete("/api/cart/items/{menuId}", menu1.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].menuId").value(menu2.getId()))
                .andExpect(jsonPath("$.totalAmount").value(4000));
    }

    @Test
    @DisplayName("장바구니 비우기 성공")
    void clearCart_Success() throws Exception {
        // given
        CartItemRequest request = CartItemRequest.builder()
                .menuId(menu1.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(delete("/api/cart")
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 장바구니 확인
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalAmount").value(0));
    }

    @Test
    @DisplayName("장바구니 아이템 개수 조회")
    void getCartItemCount_Success() throws Exception {
        // given
        CartItemRequest request1 = CartItemRequest.builder()
                .menuId(menu1.getId())
                .quantity(2)
                .build();

        CartItemRequest request2 = CartItemRequest.builder()
                .menuId(menu2.getId())
                .quantity(3)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(get("/api/cart/count")
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2)); // 2개의 다른 메뉴
    }

    @Test
    @DisplayName("장바구니에 품절 메뉴 추가 실패")
    void addToCart_SoldOutMenu_Fail() throws Exception {
        // given
        menu1.updateStatus(MenuStatus.SOLD_OUT);
        menuRepository.save(menu1);

        CartItemRequest request = CartItemRequest.builder()
                .menuId(menu1.getId())
                .quantity(1)
                .build();

        // when & then
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("최소 주문 금액 미달 확인")
    void getCart_BelowMinimumOrderAmount() throws Exception {
        // given
        CartItemRequest request = CartItemRequest.builder()
                .menuId(menu2.getId()) // 콜라 2000원
                .quantity(2) // 총 4000원 (최소 주문금액 15000원 미달)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(4000))
                .andExpect(jsonPath("$.canOrder").value(false))
                .andExpect(jsonPath("$.cannotOrderReason").exists());
    }
}