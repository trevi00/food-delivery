package com.portfolio.food_delivery.domain.review.service;

import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import com.portfolio.food_delivery.domain.order.exception.OrderNotFoundException;
import com.portfolio.food_delivery.domain.order.repository.OrderRepository;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.exception.RestaurantNotFoundException;
import com.portfolio.food_delivery.domain.restaurant.exception.UnauthorizedException;
import com.portfolio.food_delivery.domain.restaurant.repository.RestaurantRepository;
import com.portfolio.food_delivery.domain.review.dto.*;
import com.portfolio.food_delivery.domain.review.entity.Review;
import com.portfolio.food_delivery.domain.review.exception.*;
import com.portfolio.food_delivery.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {
        // 주문 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        // 권한 확인 (주문자만 리뷰 작성 가능)
        if (!order.isOwnedBy(userId)) {
            throw new UnauthorizedException("해당 주문에 대한 리뷰 작성 권한이 없습니다.");
        }

        // 배달 완료 확인
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new InvalidReviewException("배달 완료된 주문에만 리뷰를 작성할 수 있습니다.");
        }

        // 중복 리뷰 확인
        if (reviewRepository.existsByOrderIdAndIsDeletedFalse(request.getOrderId())) {
            throw new DuplicateReviewException("이미 리뷰를 작성한 주문입니다.");
        }

        // 리뷰 생성
        Review review = Review.builder()
                .user(order.getUser())
                .restaurant(order.getRestaurant())
                .order(order)
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        Review savedReview = reviewRepository.save(review);

        // 레스토랑 평점 업데이트
        updateRestaurantRating(order.getRestaurant().getId());

        return ReviewResponse.from(savedReview);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long userId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));

        // 권한 확인
        if (!review.isOwnedBy(userId)) {
            throw new UnauthorizedException("리뷰 수정 권한이 없습니다.");
        }

        // 삭제된 리뷰 확인
        if (review.getIsDeleted()) {
            throw new InvalidReviewException("삭제된 리뷰는 수정할 수 없습니다.");
        }

        // 리뷰 수정
        review.updateContent(request.getContent(), request.getRating());

        // 레스토랑 평점 업데이트
        updateRestaurantRating(review.getRestaurant().getId());

        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));

        // 권한 확인
        if (!review.isOwnedBy(userId)) {
            throw new UnauthorizedException("리뷰 삭제 권한이 없습니다.");
        }

        // 이미 삭제된 리뷰 확인
        if (review.getIsDeleted()) {
            throw new InvalidReviewException("이미 삭제된 리뷰입니다.");
        }

        // 소프트 삭제
        review.delete();

        // 레스토랑 평점 업데이트
        updateRestaurantRating(review.getRestaurant().getId());
    }

    @Transactional
    public ReviewResponse addReply(Long reviewId, Long userId, ReviewReplyRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));

        // 권한 확인 (레스토랑 오너만 답변 가능)
        if (!review.isRestaurantOwnedBy(userId)) {
            throw new UnauthorizedException("리뷰 답변 권한이 없습니다.");
        }

        // 삭제된 리뷰 확인
        if (review.getIsDeleted()) {
            throw new InvalidReviewException("삭제된 리뷰에는 답변할 수 없습니다.");
        }

        // 답변 등록
        review.addReply(request.getReply());

        return ReviewResponse.from(review);
    }

    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));

        return ReviewResponse.from(review);
    }

    public Page<ReviewResponse> getRestaurantReviews(Long restaurantId, Pageable pageable) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException("레스토랑을 찾을 수 없습니다.");
        }

        Page<Review> reviews = reviewRepository.findByRestaurantIdAndIsDeletedFalseOrderByCreatedAtDesc(
                restaurantId, pageable);

        return reviews.map(ReviewResponse::from);
    }

    public Page<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(
                userId, pageable);

        return reviews.map(ReviewResponse::from);
    }

    public Map<String, Object> getRestaurantReviewStats(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException("레스토랑을 찾을 수 없습니다.");
        }

        Map<String, Object> stats = new HashMap<>();

        // 평균 평점
        Double averageRating = reviewRepository.calculateAverageRating(restaurantId);
        stats.put("averageRating", averageRating != null ? averageRating : 0.0);

        // 총 리뷰 수
        Integer totalReviews = reviewRepository.countByRestaurantId(restaurantId);
        stats.put("totalReviews", totalReviews);

        // 평점별 개수
        List<Object[]> ratingCounts = reviewRepository.countByRating(restaurantId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        for (Object[] row : ratingCounts) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingDistribution.put(rating, count);
        }
        stats.put("ratingDistribution", ratingDistribution);

        return stats;
    }

    private void updateRestaurantRating(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("레스토랑을 찾을 수 없습니다."));

        Double averageRating = reviewRepository.calculateAverageRating(restaurantId);
        Integer reviewCount = reviewRepository.countByRestaurantId(restaurantId);

        restaurant.updateRating(
                averageRating != null ? averageRating : 0.0,
                reviewCount != null ? reviewCount : 0
        );

        restaurantRepository.save(restaurant);

        log.info("레스토랑 {} 평점 업데이트: {} (리뷰 {}개)",
                restaurantId, averageRating, reviewCount);
    }
}