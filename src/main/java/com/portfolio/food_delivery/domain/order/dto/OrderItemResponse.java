package com.portfolio.food_delivery.domain.order.dto;

import com.portfolio.food_delivery.domain.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private Long menuId;
    private String menuName;
    private Integer quantity;
    private Integer price;
    private Integer subtotal;

    public static OrderItemResponse from(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .menuId(orderItem.getMenu().getId())
                .menuName(orderItem.getMenu().getName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .subtotal(orderItem.getSubtotal())
                .build();
    }
}