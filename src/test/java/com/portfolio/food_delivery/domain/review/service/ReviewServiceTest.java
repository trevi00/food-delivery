package com.portfolio.food_delivery.domain.review.service;

import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import com.portfolio.food_delivery.domain.order.repository.OrderRepository;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.repository.RestaurantRepository;
import com.portfolio.food_delivery.domain.review.dto.ReviewCreateRequest;
import com.portfolio.food_delivery.domain.review.dto.ReviewReplyRequest;
import com.portfolio.food_delivery.domain.review.dto.ReviewResponse;
import com.portfolio.food_delivery.domain.review.dto.ReviewUpdateRequest;
import com.portfolio.food_delivery.domain.review.entity.Review;
import com.portfolio.food_delivery.domain.review.exception.DuplicateReviewException;
import com.portfolio.food_delivery.domain.review.exception.InvalidReviewException;
import com.portfolio.food_delivery.domain.review.exception.ReviewNotFoundException;
import com.portfolio.food_delivery.domain.review.repository.ReviewRepository;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("리뷰 작성 성공")
    void createReview_Success() {
        // given
        Long userId = 1L;
        Long orderId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Order order = createDeliveredOrder(orderId, user, restaurant);

        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .orderId(orderId)
                .rating(5)
                .content("정말 맛있었어요!")
                .imageUrl("https://example.com/review.jpg")
                .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(reviewRepository.existsByOrderIdAndIsDeletedFalse(orderId)).willReturn(false);
        given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            return Review.builder()
                    .id(1L)
                    .user(review.getUser())
                    .restaurant(review.getRestaurant())
                    .order(review.getOrder())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .imageUrl(review.getImageUrl())
                    .isDeleted(false)
                    .build();
        });
        given(reviewRepository.calculateAverageRating(restaurant.getId())).willReturn(4.5);
        given(reviewRepository.countByRestaurantId(restaurant.getId())).willReturn(10);
        given(restaurantRepository.findById(restaurant.getId())).willReturn(Optional.of(restaurant));
        given(restaurantRepository.save(any(Restaurant.class))).willReturn(restaurant);

        // when
        ReviewResponse response = reviewService.createReview(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getContent()).isEqualTo("정말 맛있었어요!");

        verify(orderRepository).findById(orderId);
        verify(reviewRepository).existsByOrderIdAndIsDeletedFalse(orderId);
        verify(reviewRepository).save(any(Review.class));
        verify(restaurantRepository).findById(restaurant.getId());
        verify(restaurantRepository).save(any(Restaurant.class)); // 평점 업데이트
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 배달 완료되지 않은 주문")
    void createReview_NotDeliveredOrder_Fail() {
        // given
        Long userId = 1L;
        Long orderId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Order order = createPendingOrder(orderId, user, restaurant);

        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .orderId(orderId)
                .rating(5)
                .content("맛있어요")
                .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(userId, request))
                .isInstanceOf(InvalidReviewException.class)
                .hasMessage("배달 완료된 주문에만 리뷰를 작성할 수 있습니다.");
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 이미 작성한 리뷰")
    void createReview_DuplicateReview_Fail() {
        // given
        Long userId = 1L;
        Long orderId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Order order = createDeliveredOrder(orderId, user, restaurant);

        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .orderId(orderId)
                .rating(5)
                .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(reviewRepository.existsByOrderIdAndIsDeletedFalse(orderId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(userId, request))
                .isInstanceOf(DuplicateReviewException.class)
                .hasMessage("이미 리뷰를 작성한 주문입니다.");
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_Success() {
        // given
        Long reviewId = 1L;
        Long userId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Order order = createDeliveredOrder(1L, user, restaurant);
        Review review = createReview(reviewId, user, restaurant, order);

        ReviewUpdateRequest request = ReviewUpdateRequest.builder()
                .rating(4)
                .content("수정된 내용")
                .build();

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(reviewRepository.calculateAverageRating(restaurant.getId())).willReturn(4.2);
        given(reviewRepository.countByRestaurantId(restaurant.getId())).willReturn(10);
        given(restaurantRepository.findById(restaurant.getId())).willReturn(Optional.of(restaurant));
        given(restaurantRepository.save(any(Restaurant.class))).willReturn(restaurant);

        // when
        ReviewResponse response = reviewService.updateReview(reviewId, userId, request);

        // then
        assertThat(response.getRating()).isEqualTo(4);
        assertThat(response.getContent()).isEqualTo("수정된 내용");

        verify(reviewRepository).findById(reviewId);
        verify(restaurantRepository).findById(restaurant.getId());
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("사장님 답변 등록 성공")
    void addReply_Success() {
        // given
        Long reviewId = 1L;
        Long ownerId = 2L;

        User customer = createUser(1L);
        User owner = createOwner(ownerId);
        Restaurant restaurant = createRestaurantWithOwner(owner);
        Order order = createDeliveredOrder(1L, customer, restaurant);
        Review review = createReview(reviewId, customer, restaurant, order);

        ReviewReplyRequest request = ReviewReplyRequest.builder()
                .reply("소중한 리뷰 감사합니다!")
                .build();

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when
        ReviewResponse response = reviewService.addReply(reviewId, ownerId, request);

        // then
        assertThat(response.getReply()).isEqualTo("소중한 리뷰 감사합니다!");
        assertThat(response.getRepliedAt()).isNotNull();

        verify(reviewRepository).findById(reviewId);
    }

    @Test
    @DisplayName("레스토랑 리뷰 목록 조회")
    void getRestaurantReviews_Success() {
        // given
        Long restaurantId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Restaurant restaurant = createRestaurant();
        User user1 = createUser(1L);
        User user2 = createUser(2L);
        Order order1 = createDeliveredOrder(1L, user1, restaurant);
        Order order2 = createDeliveredOrder(2L, user2, restaurant);

        Review review1 = createReview(1L, user1, restaurant, order1);
        Review review2 = createReview(2L, user2, restaurant, order2);

        Page<Review> reviewPage = new PageImpl<>(Arrays.asList(review1, review2), pageable, 2);

        given(restaurantRepository.existsById(restaurantId)).willReturn(true);
        given(reviewRepository.findByRestaurantIdAndIsDeletedFalseOrderByCreatedAtDesc(restaurantId, pageable))
                .willReturn(reviewPage);

        // when
        Page<ReviewResponse> response = reviewService.getRestaurantReviews(restaurantId, pageable);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);

        verify(restaurantRepository).existsById(restaurantId);
        verify(reviewRepository).findByRestaurantIdAndIsDeletedFalseOrderByCreatedAtDesc(restaurantId, pageable);
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_Success() {
        // given
        Long reviewId = 1L;
        Long userId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Review review = createReview(reviewId, user, restaurant, null);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(reviewRepository.calculateAverageRating(restaurant.getId())).willReturn(4.0);
        given(reviewRepository.countByRestaurantId(restaurant.getId())).willReturn(9);
        given(restaurantRepository.findById(restaurant.getId())).willReturn(Optional.of(restaurant));
        given(restaurantRepository.save(any(Restaurant.class))).willReturn(restaurant);

        // when
        reviewService.deleteReview(reviewId, userId);

        // then
        assertThat(review.getIsDeleted()).isTrue();

        verify(reviewRepository).findById(reviewId);
        verify(restaurantRepository).findById(restaurant.getId());
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    // Helper methods
    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .name("사용자" + id)
                .role(UserRole.CUSTOMER)
                .build();
    }

    private User createOwner(Long id) {
        return User.builder()
                .id(id)
                .email("owner" + id + "@example.com")
                .name("사장님" + id)
                .role(UserRole.RESTAURANT_OWNER)
                .build();
    }

    private Restaurant createRestaurant() {
        return Restaurant.builder()
                .id(1L)
                .owner(createOwner(2L))
                .name("맛있는 치킨")
                .rating(4.5)
                .reviewCount(100)
                .build();
    }

    private Restaurant createRestaurantWithOwner(User owner) {
        return Restaurant.builder()
                .id(1L)
                .owner(owner)
                .name("맛있는 치킨")
                .rating(4.5)
                .reviewCount(100)
                .build();
    }

    private Order createDeliveredOrder(Long id, User user, Restaurant restaurant) {
        return Order.builder()
                .id(id)
                .user(user)
                .restaurant(restaurant)
                .status(OrderStatus.DELIVERED)
                .completedAt(LocalDateTime.now().minusHours(1))
                .build();
    }

    private Order createPendingOrder(Long id, User user, Restaurant restaurant) {
        return Order.builder()
                .id(id)
                .user(user)
                .restaurant(restaurant)
                .status(OrderStatus.PENDING)
                .build();
    }

    private Review createReview(Long id, User user, Restaurant restaurant, Order order) {
        return Review.builder()
                .id(id)
                .user(user)
                .restaurant(restaurant)
                .order(order)
                .rating(5)
                .content("맛있어요!")
                .isDeleted(false)
                .build();
    }
}