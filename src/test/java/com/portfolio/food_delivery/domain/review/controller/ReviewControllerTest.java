package com.portfolio.food_delivery.domain.review.controller;

import com.portfolio.food_delivery.common.BaseIntegrationTest;
import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.order.entity.OrderItem;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import com.portfolio.food_delivery.domain.order.repository.OrderRepository;
import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.repository.MenuRepository;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.restaurant.repository.RestaurantRepository;
import com.portfolio.food_delivery.domain.review.dto.ReviewCreateRequest;
import com.portfolio.food_delivery.domain.review.dto.ReviewReplyRequest;
import com.portfolio.food_delivery.domain.review.dto.ReviewUpdateRequest;
import com.portfolio.food_delivery.domain.review.entity.Review;
import com.portfolio.food_delivery.domain.review.repository.ReviewRepository;
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

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String customerToken;
    private String ownerToken;
    private User customer;
    private User owner;
    private Restaurant restaurant;
    private Order deliveredOrder;

    @BeforeEach
    void setUp() throws Exception {
        reviewRepository.deleteAll();
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
                .rating(0.0)
                .reviewCount(0)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        // 메뉴 생성
        Menu menu = Menu.builder()
                .restaurant(restaurant)
                .name("양념치킨")
                .price(20000)
                .build();
        menu = menuRepository.save(menu);

        // 배달 완료된 주문 생성
        deliveredOrder = Order.builder()
                .user(customer)
                .restaurant(restaurant)
                .deliveryAddress(customer.getAddress())
                .phoneNumber(customer.getPhoneNumber())
                .totalAmount(20000)
                .deliveryFee(3000)
                .status(OrderStatus.DELIVERED)
                .orderedAt(LocalDateTime.now().minusHours(2))
                .completedAt(LocalDateTime.now().minusHours(1))
                .build();

        OrderItem orderItem = OrderItem.builder()
                .menu(menu)
                .quantity(1)
                .price(menu.getPrice())
                .build();
        deliveredOrder.addOrderItem(orderItem);

        deliveredOrder = orderRepository.save(deliveredOrder);

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
    @DisplayName("리뷰 작성 성공")
    void createReview_Success() throws Exception {
        // given
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .orderId(deliveredOrder.getId())
                .rating(5)
                .content("정말 맛있었어요! 재주문 의사 100%입니다.")
                .imageUrl("https://example.com/review.jpg")
                .build();

        // when & then
        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("정말 맛있었어요! 재주문 의사 100%입니다."))
                .andExpect(jsonPath("$.userName").value("고객님"))
                .andExpect(jsonPath("$.restaurantName").value("맛있는 치킨"));

        // 레스토랑 평점 업데이트 확인
        Restaurant updatedRestaurant = restaurantRepository.findById(restaurant.getId()).orElseThrow();
        assertThat(updatedRestaurant.getRating()).isEqualTo(5.0);
        assertThat(updatedRestaurant.getReviewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 배달 완료되지 않은 주문")
    void createReview_NotDeliveredOrder_BadRequest() throws Exception {
        // given
        Order pendingOrder = Order.builder()
                .user(customer)
                .restaurant(restaurant)
                .deliveryAddress(customer.getAddress())
                .phoneNumber(customer.getPhoneNumber())
                .status(OrderStatus.PENDING)
                .orderedAt(LocalDateTime.now())
                .totalAmount(20000)
                .deliveryFee(3000)
                .build();
        pendingOrder = orderRepository.save(pendingOrder);

        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .orderId(pendingOrder.getId())
                .rating(5)
                .content("맛있어요")
                .build();

        // when & then
        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."));
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_Success() throws Exception {
        // given
        Review review = Review.builder()
                .user(customer)
                .restaurant(restaurant)
                .order(deliveredOrder)
                .rating(5)
                .content("맛있어요")
                .build();
        review = reviewRepository.save(review);

        ReviewUpdateRequest request = ReviewUpdateRequest.builder()
                .rating(4)
                .content("맛은 있는데 양이 좀 적어요")
                .build();

        // when & then
        mockMvc.perform(put("/api/reviews/{reviewId}", review.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.content").value("맛은 있는데 양이 좀 적어요"));
    }

    @Test
    @DisplayName("사장님 답변 등록 성공")
    void addReply_Success() throws Exception {
        // given
        Review review = Review.builder()
                .user(customer)
                .restaurant(restaurant)
                .order(deliveredOrder)
                .rating(5)
                .content("맛있어요")
                .build();
        review = reviewRepository.save(review);

        ReviewReplyRequest request = ReviewReplyRequest.builder()
                .reply("소중한 리뷰 감사합니다! 앞으로도 맛있는 음식으로 보답하겠습니다.")
                .build();

        // when & then
        mockMvc.perform(post("/api/reviews/{reviewId}/reply", review.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("소중한 리뷰 감사합니다! 앞으로도 맛있는 음식으로 보답하겠습니다."))
                .andExpect(jsonPath("$.repliedAt").exists());
    }

    @Test
    @DisplayName("레스토랑 리뷰 목록 조회")
    void getRestaurantReviews_Success() throws Exception {
        // given
        Review review1 = Review.builder()
                .user(customer)
                .restaurant(restaurant)
                .order(deliveredOrder)
                .rating(5)
                .content("정말 맛있어요!")
                .build();
        reviewRepository.save(review1);

        // 다른 고객의 리뷰
        User customer2 = User.builder()
                .email("customer2@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("고객2")
                .phoneNumber("010-5555-6666")
                .role(UserRole.CUSTOMER)
                .address(new Address("서울시", "강남구", "역삼로", "789", "12347"))
                .build();
        customer2 = userRepository.save(customer2);

        Order order2 = Order.builder()
                .user(customer2)
                .restaurant(restaurant)
                .deliveryAddress(customer2.getAddress())
                .phoneNumber(customer2.getPhoneNumber())
                .status(OrderStatus.DELIVERED)
                .orderedAt(LocalDateTime.now().minusHours(3))
                .completedAt(LocalDateTime.now().minusHours(2))
                .totalAmount(20000)
                .deliveryFee(3000)
                .build();
        order2 = orderRepository.save(order2);

        Review review2 = Review.builder()
                .user(customer2)
                .restaurant(restaurant)
                .order(order2)
                .rating(4)
                .content("맛있는데 배달이 좀 늦었어요")
                .build();
        reviewRepository.save(review2);

        // when & then
        mockMvc.perform(get("/api/reviews/restaurants/{restaurantId}", restaurant.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("레스토랑 리뷰 통계 조회")
    void getRestaurantReviewStats_Success() throws Exception {
        // given
        createMultipleReviews();

        // when & then
        mockMvc.perform(get("/api/reviews/restaurants/{restaurantId}/stats", restaurant.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").exists())
                .andExpect(jsonPath("$.totalReviews").value(3))
                .andExpect(jsonPath("$.ratingDistribution").exists())
                .andExpect(jsonPath("$.ratingDistribution.5").value(2))
                .andExpect(jsonPath("$.ratingDistribution.4").value(1));
    }

    @Test
    @DisplayName("내 리뷰 목록 조회")
    void getMyReviews_Success() throws Exception {
        // given
        Review review = Review.builder()
                .user(customer)
                .restaurant(restaurant)
                .order(deliveredOrder)
                .rating(5)
                .content("맛있어요!")
                .build();
        reviewRepository.save(review);

        // when & then
        mockMvc.perform(get("/api/reviews/my")
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].rating").value(5));
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_Success() throws Exception {
        // given
        Review review = Review.builder()
                .user(customer)
                .restaurant(restaurant)
                .order(deliveredOrder)
                .rating(5)
                .content("맛있어요!")
                .build();
        review = reviewRepository.save(review);

        // when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}", review.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 소프트 삭제 확인
        Review deletedReview = reviewRepository.findById(review.getId()).orElseThrow();
        assertThat(deletedReview.getIsDeleted()).isTrue();
    }

    private void createMultipleReviews() {
        // 5점 리뷰 2개
        for (int i = 0; i < 2; i++) {
            User user = User.builder()
                    .email("user" + i + "@example.com")
                    .password(passwordEncoder.encode("password123!"))
                    .name("사용자" + i)
                    .phoneNumber("010-0000-000" + i)
                    .role(UserRole.CUSTOMER)
                    .build();
            user = userRepository.save(user);

            Order order = Order.builder()
                    .user(user)
                    .restaurant(restaurant)
                    .deliveryAddress(new Address("서울시", "강남구", "선릉로", "100", "12348"))
                    .phoneNumber(user.getPhoneNumber())
                    .status(OrderStatus.DELIVERED)
                    .orderedAt(LocalDateTime.now().minusHours(i + 3))
                    .completedAt(LocalDateTime.now().minusHours(i + 2))
                    .totalAmount(20000)
                    .deliveryFee(3000)
                    .build();
            order = orderRepository.save(order);

            Review review = Review.builder()
                    .user(user)
                    .restaurant(restaurant)
                    .order(order)
                    .rating(5)
                    .content("맛있어요!")
                    .build();
            reviewRepository.save(review);
        }

        // 4점 리뷰 1개
        User user3 = User.builder()
                .email("user3@example.com")
                .password(passwordEncoder.encode("password123!"))
                .name("사용자3")
                .phoneNumber("010-0000-0003")
                .role(UserRole.CUSTOMER)
                .build();
        user3 = userRepository.save(user3);

        Order order3 = Order.builder()
                .user(user3)
                .restaurant(restaurant)
                .deliveryAddress(new Address("서울시", "강남구", "대치로", "200", "12349"))
                .phoneNumber(user3.getPhoneNumber())
                .status(OrderStatus.DELIVERED)
                .orderedAt(LocalDateTime.now().minusHours(5))
                .completedAt(LocalDateTime.now().minusHours(4))
                .totalAmount(20000)
                .deliveryFee(3000)
                .build();
        order3 = orderRepository.save(order3);

        Review review3 = Review.builder()
                .user(user3)
                .restaurant(restaurant)
                .order(order3)
                .rating(4)
                .content("괜찮아요")
                .build();
        reviewRepository.save(review3);
    }
}