package com.portfolio.food_delivery.domain.restaurant.dto;

import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RestaurantCreateRequest {

    @NotBlank(message = "레스토랑 이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "레스토랑 이름은 2자 이상 50자 이하여야 합니다.")
    private String name;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    private String description;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phoneNumber;

    @NotNull(message = "주소는 필수입니다.")
    private Address address;

    @NotNull(message = "카테고리는 필수입니다.")
    private RestaurantCategory category;

    @NotNull(message = "오픈 시간은 필수입니다.")
    private LocalTime openTime;

    @NotNull(message = "마감 시간은 필수입니다.")
    private LocalTime closeTime;

    @NotNull(message = "최소 주문 금액은 필수입니다.")
    @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
    private Integer minimumOrderAmount;

    @NotNull(message = "배달료는 필수입니다.")
    @Min(value = 0, message = "배달료는 0원 이상이어야 합니다.")
    private Integer deliveryFee;
}