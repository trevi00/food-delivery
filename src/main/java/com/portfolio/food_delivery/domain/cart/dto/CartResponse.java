// CartResponse.java
package com.portfolio.food_delivery.domain.cart.dto;

import com.portfolio.food_delivery.domain.cart.entity.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private Integer deliveryFee;
    private Integer minimumOrderAmount;
    private List<CartItemResponse> items;
    private Integer totalAmount;
    private Integer totalQuantity;
    private Boolean canOrder;
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