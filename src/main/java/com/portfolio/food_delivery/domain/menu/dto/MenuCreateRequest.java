package com.portfolio.food_delivery.domain.menu.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MenuCreateRequest {

    @NotBlank(message = "메뉴명은 필수입니다.")
    @Size(min = 2, max = 50, message = "메뉴명은 2자 이상 50자 이하여야 합니다.")
    private String name;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    @Max(value = 1000000, message = "가격은 1,000,000원 이하여야 합니다.")
    private Integer price;

    private String imageUrl;
}