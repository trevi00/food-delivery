package com.portfolio.food_delivery.domain.restaurant.dto;

import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;

@Schema(description = "레스토랑 등록 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RestaurantCreateRequest {

    @Schema(description = "레스토랑 이름", example = "맛있는 치킨", minLength = 2, maxLength = 50, required = true)
    @NotBlank(message = "레스토랑 이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "레스토랑 이름은 2자 이상 50자 이하여야 합니다.")
    private String name;

    @Schema(description = "레스토랑 설명", example = "바삭바삭한 후라이드와 양념치킨 전문점", maxLength = 500)
    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    private String description;

    @Schema(description = "전화번호", example = "02-1234-5678", required = true)
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phoneNumber;

    @Schema(description = "레스토랑 주소", required = true)
    @NotNull(message = "주소는 필수입니다.")
    private Address address;

    @Schema(description = "레스토랑 카테고리", example = "CHICKEN", required = true,
            allowableValues = {"KOREAN", "JAPANESE", "CHINESE", "WESTERN", "CHICKEN", "PIZZA", "BURGER", "CAFE", "ASIAN", "SNACK", "MIDNIGHT", "OTHER"})
    @NotNull(message = "카테고리는 필수입니다.")
    private RestaurantCategory category;

    @Schema(description = "오픈 시간", example = "10:00", required = true)
    @NotNull(message = "오픈 시간은 필수입니다.")
    private LocalTime openTime;

    @Schema(description = "마감 시간", example = "22:00", required = true)
    @NotNull(message = "마감 시간은 필수입니다.")
    private LocalTime closeTime;

    @Schema(description = "최소 주문 금액", example = "15000", minimum = "0", required = true)
    @NotNull(message = "최소 주문 금액은 필수입니다.")
    @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
    private Integer minimumOrderAmount;

    @Schema(description = "배달료", example = "3000", minimum = "0", required = true)
    @NotNull(message = "배달료는 필수입니다.")
    @Min(value = 0, message = "배달료는 0원 이상이어야 합니다.")
    private Integer deliveryFee;
}