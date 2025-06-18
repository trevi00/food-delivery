package com.portfolio.food_delivery.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Schema(description = "리뷰 작성 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewCreateRequest {

    @Schema(description = "주문 ID", example = "1", required = true)
    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;

    @Schema(description = "평점 (1-5)", example = "5", minimum = "1", maximum = "5", required = true)
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private Integer rating;

    @Schema(description = "리뷰 내용", example = "정말 맛있었어요! 재주문 의사 100%입니다.", maxLength = 1000)
    @Size(max = 1000, message = "리뷰 내용은 1000자 이하여야 합니다.")
    private String content;

    @Schema(description = "리뷰 이미지 URL", example = "https://example.com/review-image.jpg")
    private String imageUrl;
}