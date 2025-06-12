package com.portfolio.food_delivery.domain.review.controller;

import com.portfolio.food_delivery.domain.review.dto.*;
import com.portfolio.food_delivery.domain.review.service.ReviewService;
import com.portfolio.food_delivery.domain.user.service.UserService;
import com.portfolio.food_delivery.infrastructure.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        ReviewResponse response = reviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long reviewId) {
        ReviewResponse response = reviewService.getReview(reviewId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        ReviewResponse response = reviewService.updateReview(reviewId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewResponse> addReply(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewReplyRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        ReviewResponse response = reviewService.addReply(reviewId, userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Page<ReviewResponse>> getRestaurantReviews(
            @PathVariable Long restaurantId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getRestaurantReviews(restaurantId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurants/{restaurantId}/stats")
    public ResponseEntity<Map<String, Object>> getRestaurantReviewStats(@PathVariable Long restaurantId) {
        Map<String, Object> stats = reviewService.getRestaurantReviewStats(restaurantId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        Page<ReviewResponse> response = reviewService.getMyReviews(userId, pageable);
        return ResponseEntity.ok(response);
    }
}