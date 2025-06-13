// CartItemResponse.java
package com.portfolio.food_delivery.domain.cart.dto;

import com.portfolio.food_delivery.domain.cart.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long id;
    private Long menuId;
    private String menuName;
    private Integer menuPrice;
    private String menuImageUrl;
    private Integer quantity;
    private Integer subtotal;

    public static CartItemResponse from(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .menuId(cartItem.getMenu().getId())
                .menuName(cartItem.getMenu().getName())
                .menuPrice(cartItem.getMenu().getPrice())
                .menuImageUrl(cartItem.getMenu().getImageUrl())
                .quantity(cartItem.getQuantity())
                .subtotal(cartItem.getSubtotal())
                .build();
    }
}
