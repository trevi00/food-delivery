package com.portfolio.food_delivery.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewReplyRequest {

    @NotBlank(message = "답변 내용은 필수입니다.")
    @Size(max = 500, message = "답변은 500자 이하여야 합니다.")
    private String reply;
}