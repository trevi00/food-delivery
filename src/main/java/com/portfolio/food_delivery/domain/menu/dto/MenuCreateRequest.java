package com.portfolio.food_delivery.domain.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Schema(description = "메뉴 등록 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MenuCreateRequest {

    @Schema(description = "메뉴명", example = "양념치킨", minLength = 2, maxLength = 50, required = true)
    @NotBlank(message = "메뉴명은 필수입니다.")
    @Size(min = 2, max = 50, message = "메뉴명은 2자 이상 50자 이하여야 합니다.")
    private String name;

    @Schema(description = "메뉴 설명", example = "특제 양념 소스를 사용한 바삭한 치킨", maxLength = 500)
    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    private String description;

    @Schema(description = "가격", example = "20000", minimum = "0", maximum = "1000000", required = true)
    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    @Max(value = 1000000, message = "가격은 1,000,000원 이하여야 합니다.")
    private Integer price;

    @Schema(description = "메뉴 이미지 URL", example = "https://example.com/menu/yangnyeom.jpg")
    private String imageUrl;
}