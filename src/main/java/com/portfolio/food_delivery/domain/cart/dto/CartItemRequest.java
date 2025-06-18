package com.portfolio.food_delivery.domain.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Schema(description = "장바구니 아이템 추가 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CartItemRequest {

    @Schema(description = "메뉴 ID", example = "1", required = true)
    @NotNull(message = "메뉴 ID는 필수입니다.")
    private Long menuId;

    @Schema(description = "수량", example = "2", minimum = "1", required = true)
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Integer quantity;
}