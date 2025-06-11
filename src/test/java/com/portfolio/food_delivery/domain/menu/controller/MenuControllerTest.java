package com.portfolio.food_delivery.domain.menu.controller;

import com.portfolio.food_delivery.common.BaseIntegrationTest;
import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.menu.dto.MenuCreateRequest;
import com.portfolio.food_delivery.domain.menu.dto.MenuUpdateRequest;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;

class MenuControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String ownerToken;
    private String customerToken;
    private User owner;
    private User customer;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() throws Exception {
        menuRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

        // 레스토랑 오너 생성
        owner = User.builder()
                .email("owner@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("사장님")
                .phoneNumber("010-1111-2222")
                .role(UserRole.RESTAURANT_OWNER)
                .build();
        owner = userRepository.save(owner);

        // 일반 고객 생성
        customer = User.builder()
                .email("customer@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("고객님")
                .phoneNumber("010-3333-4444")
                .role(UserRole.CUSTOMER)
                .build();
        customer = userRepository.save(customer);

        // 레스토랑 생성
        restaurant = Restaurant.builder()
                .owner(owner)
                .name("맛있는 치킨")
                .category(RestaurantCategory.CHICKEN)
                .phoneNumber("02-1234-5678")
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(22, 0))
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        // 토큰 발급
        ownerToken = getAccessToken("owner@example.com", "password123!");
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
    @DisplayName("메뉴 등록 성공")
    void createMenu_Success() throws Exception {
        // given
        MenuCreateRequest request = MenuCreateRequest.builder()
                .name("양념치킨")
                .description("특제 양념 소스를 사용한 치킨")
                .price(20000)
                .imageUrl("https://example.com/yangnyeom.jpg")
                .build();

        // when & then
        mockMvc.perform(post("/api/restaurants/{restaurantId}/menus", restaurant.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("양념치킨"))
                .andExpect(jsonPath("$.price").value(20000))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.displayOrder").value(1));
    }

    @Test
    @DisplayName("메뉴 등록 실패 - 권한 없음")
    void createMenu_Unauthorized() throws Exception {
        // given
        MenuCreateRequest request = MenuCreateRequest.builder()
                .name("양념치킨")
                .price(20000)
                .build();

        // when & then
        mockMvc.perform(post("/api/restaurants/{restaurantId}/menus", restaurant.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("레스토랑에 대한 권한이 없습니다."));
    }

    @Test
    @DisplayName("레스토랑의 메뉴 목록 조회")
    void getMenusByRestaurant_Success() throws Exception {
        // given
        Menu menu1 = Menu.builder()
                .restaurant(restaurant)
                .name("양념치킨")
                .price(20000)
                .displayOrder(1)
                .build();
        menuRepository.save(menu1);

        Menu menu2 = Menu.builder()
                .restaurant(restaurant)
                .name("후라이드치킨")
                .price(18000)
                .displayOrder(2)
                .build();
        menuRepository.save(menu2);

        // when & then
        mockMvc.perform(get("/api/restaurants/{restaurantId}/menus", restaurant.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("양념치킨"))
                .andExpect(jsonPath("$[1].name").value("후라이드치킨"));
    }

    @Test
    @DisplayName("메뉴 수정 성공")
    void updateMenu_Success() throws Exception {
        // given
        Menu menu = Menu.builder()
                .restaurant(restaurant)
                .name("양념치킨")
                .price(20000)
                .build();
        menu = menuRepository.save(menu);

        MenuUpdateRequest request = MenuUpdateRequest.builder()
                .name("특제양념치킨")
                .price(22000)
                .description("더욱 맛있어진 양념치킨")
                .build();

        // when & then
        mockMvc.perform(put("/api/menus/{menuId}", menu.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("특제양념치킨"))
                .andExpect(jsonPath("$.price").value(22000))
                .andExpect(jsonPath("$.description").value("더욱 맛있어진 양념치킨"));
    }

    @Test
    @DisplayName("메뉴 상태 변경 - 품절 처리")
    void updateMenuStatus_Success() throws Exception {
        // given
        Menu menu = Menu.builder()
                .restaurant(restaurant)
                .name("양념치킨")
                .price(20000)
                .build();
        menu = menuRepository.save(menu);

        // when & then
        mockMvc.perform(patch("/api/menus/{menuId}/status", menu.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("status", "SOLD_OUT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLD_OUT"));
    }

    @Test
    @DisplayName("메뉴 삭제 성공")
    void deleteMenu_Success() throws Exception {
        // given
        Menu menu = Menu.builder()
                .restaurant(restaurant)
                .name("양념치킨")
                .price(20000)
                .build();
        menu = menuRepository.save(menu);

        // when & then
        mockMvc.perform(delete("/api/menus/{menuId}", menu.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 소프트 삭제 확인
        Menu deletedMenu = menuRepository.findById(menu.getId()).orElseThrow();
        assertThat(deletedMenu.getStatus()).isEqualTo(MenuStatus.DELETED);
    }
}