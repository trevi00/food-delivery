package com.portfolio.food_delivery.domain.review.dto;

import com.portfolio.food_delivery.domain.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "리뷰 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    @Schema(description = "리뷰 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자명", example = "홍길동")
    private String userName;

    @Schema(description = "레스토랑 ID", example = "1")
    private Long restaurantId;

    @Schema(description = "레스토랑명", example = "맛있는 치킨")
    private String restaurantName;

    @Schema(description = "주문 ID", example = "1")
    private Long orderId;

    @Schema(description = "평점", example = "5", minimum = "1", maximum = "5")
    private Integer rating;

    @Schema(description = "리뷰 내용", example = "정말 맛있었어요!")
    private String content;

    @Schema(description = "리뷰 이미지 URL", example = "https://example.com/review.jpg")
    private String imageUrl;

    @Schema(description = "사장님 답변", example = "감사합니다. 또 방문해주세요!")
    private String reply;

    @Schema(description = "답변 작성일시", example = "2025-01-15T15:00:00")
    private LocalDateTime repliedAt;

    @Schema(description = "리뷰 작성일시", example = "2025-01-15T14:00:00")
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .restaurantId(review.getRestaurant().getId())
                .restaurantName(review.getRestaurant().getName())
                .orderId(review.getOrder().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .imageUrl(review.getImageUrl())
                .reply(review.getReply())
                .repliedAt(review.getRepliedAt())
                .createdAt(review.getCreatedAt())
                .build();
    }
}