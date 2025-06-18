// CartResponse.java
package com.portfolio.food_delivery.domain.cart.dto;

import com.portfolio.food_delivery.domain.cart.entity.Cart;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "장바구니 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    @Schema(description = "장바구니 ID", example = "1")
    private Long id;

    @Schema(description = "레스토랑 ID", example = "1")
    private Long restaurantId;

    @Schema(description = "레스토랑명", example = "맛있는 치킨")
    private String restaurantName;

    @Schema(description = "배달료", example = "3000")
    private Integer deliveryFee;

    @Schema(description = "최소 주문 금액", example = "15000")
    private Integer minimumOrderAmount;

    @Schema(description = "장바구니 아이템 목록")
    private List<CartItemResponse> items;

    @Schema(description = "총 금액 (메뉴 금액만)", example = "25000")
    private Integer totalAmount;

    @Schema(description = "총 수량", example = "3")
    private Integer totalQuantity;

    @Schema(description = "주문 가능 여부", example = "true")
    private Boolean canOrder;

    @Schema(description = "주문 불가 사유", example = "최소 주문 금액(15,000원)을 충족하지 않습니다.")
    private String cannotOrderReason;

    public static CartResponse from(Cart cart) {
        if (cart.isEmpty()) {
            return CartResponse.builder()
                    .id(cart.getId())
                    .items(List.of())
                    .totalAmount(0)
                    .totalQuantity(0)
                    .canOrder(false)
                    .cannotOrderReason("장바구니가 비어있습니다.")
                    .build();
        }

        Integer totalAmount = cart.getTotalAmount();
        Integer minimumOrderAmount = cart.getRestaurant().getMinimumOrderAmount();
        Boolean canOrder = totalAmount >= minimumOrderAmount;
        String cannotOrderReason = null;

        if (!canOrder) {
            cannotOrderReason = String.format("최소 주문 금액(%,d원)을 충족하지 않습니다. 현재 금액: %,d원",
                    minimumOrderAmount, totalAmount);
        }

        return CartResponse.builder()
                .id(cart.getId())
                .restaurantId(cart.getRestaurant().getId())
                .restaurantName(cart.getRestaurant().getName())
                .deliveryFee(cart.getRestaurant().getDeliveryFee())
                .minimumOrderAmount(minimumOrderAmount)
                .items(cart.getCartItems().stream()
                        .map(CartItemResponse::from)
                        .collect(Collectors.toList()))
                .totalAmount(totalAmount)
                .totalQuantity(cart.getTotalQuantity())
                .canOrder(canOrder)
                .cannotOrderReason(cannotOrderReason)
                .build();
    }
}