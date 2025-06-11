package com.portfolio.food_delivery.domain.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RestaurantUpdateRequest {

    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    private String description;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phoneNumber;

    private LocalTime openTime;
    private LocalTime closeTime;

    @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
    private Integer minimumOrderAmount;

    @Min(value = 0, message = "배달료는 0원 이상이어야 합니다.")
    private Integer deliveryFee;
}