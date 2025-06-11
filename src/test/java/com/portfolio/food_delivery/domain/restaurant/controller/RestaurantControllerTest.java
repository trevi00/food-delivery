package com.portfolio.food_delivery.domain.restaurant.controller;

import com.portfolio.food_delivery.common.BaseIntegrationTest;
import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantCreateRequest;
import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantUpdateRequest;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantStatus;
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

class RestaurantControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String ownerToken;
    private String customerToken;
    private User owner;
    private User customer;

    @BeforeEach
    void setUp() throws Exception {
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

        // 레스토랑 오너 생성 및 로그인
        owner = User.builder()
                .email("owner@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("사장님")
                .phoneNumber("010-1111-2222")
                .role(UserRole.RESTAURANT_OWNER)
                .build();
        owner = userRepository.save(owner);

        // 일반 고객 생성 및 로그인
        customer = User.builder()
                .email("customer@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("고객님")
                .phoneNumber("010-3333-4444")
                .role(UserRole.CUSTOMER)
                .build();
        customer = userRepository.save(customer);

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
    @DisplayName("레스토랑 등록 성공 - 레스토랑 오너")
    void createRestaurant_Success() throws Exception {
        // given
        RestaurantCreateRequest request = RestaurantCreateRequest.builder()
                .name("맛있는 치킨")
                .description("바삭한 치킨 전문점")
                .phoneNumber("02-1234-5678")
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .category(RestaurantCategory.CHICKEN)
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(22, 0))
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();

        // when & then
        mockMvc.perform(post("/api/restaurants")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("맛있는 치킨"))
                .andExpect(jsonPath("$.category").value("CHICKEN"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.rating").value(0.0));
    }

    @Test
    @DisplayName("레스토랑 등록 실패 - 권한 없음")
    void createRestaurant_Unauthorized() throws Exception {
        // given
        RestaurantCreateRequest request = RestaurantCreateRequest.builder()
                .name("맛있는 치킨")
                .description("바삭한 치킨 전문점")
                .phoneNumber("02-1234-5678")
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .category(RestaurantCategory.CHICKEN)
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(22, 0))
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();

        // when & then
        mockMvc.perform(post("/api/restaurants")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("레스토랑에 대한 권한이 없습니다."));  // 메시지 수정
    }

    @Test
    @DisplayName("레스토랑 조회 성공")
    void getRestaurant_Success() throws Exception {
        // given
        Restaurant restaurant = Restaurant.builder()
                .owner(owner)
                .name("맛있는 치킨")
                .description("바삭한 치킨 전문점")
                .phoneNumber("02-1234-5678")
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .category(RestaurantCategory.CHICKEN)
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(22, 0))
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // when & then
        mockMvc.perform(get("/api/restaurants/{id}", savedRestaurant.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRestaurant.getId()))
                .andExpect(jsonPath("$.name").value("맛있는 치킨"))
                .andExpect(jsonPath("$.category").value("CHICKEN"));
    }

    @Test
    @DisplayName("레스토랑 목록 조회 성공")
    void getRestaurants_Success() throws Exception {
        // given
        Restaurant restaurant1 = Restaurant.builder()
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
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = Restaurant.builder()
                .owner(owner)
                .name("피자하우스")
                .category(RestaurantCategory.PIZZA)
                .phoneNumber("02-2345-6789")
                .address(new Address("서울시", "강남구", "선릉로", "456", "12346"))
                .openTime(LocalTime.of(11, 0))
                .closeTime(LocalTime.of(23, 0))
                .minimumOrderAmount(20000)
                .deliveryFee(2000)
                .build();
        restaurantRepository.save(restaurant2);

        // when & then
        mockMvc.perform(get("/api/restaurants")
                        .param("status", "OPEN")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").exists())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("레스토랑 정보 수정 성공")
    void updateRestaurant_Success() throws Exception {
        // given
        Restaurant restaurant = Restaurant.builder()
                .owner(owner)
                .name("맛있는 치킨")
                .description("바삭한 치킨 전문점")
                .phoneNumber("02-1234-5678")
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .category(RestaurantCategory.CHICKEN)
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(22, 0))
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        RestaurantUpdateRequest request = RestaurantUpdateRequest.builder()
                .description("더욱 맛있는 치킨 전문점")
                .minimumOrderAmount(20000)
                .deliveryFee(2000)
                .build();

        // when & then
        mockMvc.perform(put("/api/restaurants/{id}", savedRestaurant.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("더욱 맛있는 치킨 전문점"))
                .andExpect(jsonPath("$.minimumOrderAmount").value(20000))
                .andExpect(jsonPath("$.deliveryFee").value(2000));
    }

    @Test
    @DisplayName("레스토랑 정보 수정 실패 - 다른 사장님의 레스토랑")
    void updateRestaurant_Unauthorized() throws Exception {
        // given
        User otherOwner = User.builder()
                .email("other@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("다른사장님")
                .phoneNumber("010-5555-6666")
                .role(UserRole.RESTAURANT_OWNER)
                .build();
        otherOwner = userRepository.save(otherOwner);

        Restaurant restaurant = Restaurant.builder()
                .owner(otherOwner)
                .name("다른 치킨집")
                .category(RestaurantCategory.CHICKEN)
                .phoneNumber("02-1234-5678")
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(22, 0))
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        RestaurantUpdateRequest request = RestaurantUpdateRequest.builder()
                .description("수정하면 안되는 설명")
                .build();

        // when & then
        mockMvc.perform(put("/api/restaurants/{id}", savedRestaurant.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("레스토랑에 대한 권한이 없습니다."));  // 메시지 수정
    }
}