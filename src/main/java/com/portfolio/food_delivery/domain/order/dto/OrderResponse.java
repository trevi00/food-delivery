package com.portfolio.food_delivery.domain.order.dto;

import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private String restaurantName;
    private List<OrderItemResponse> orderItems;
    private Address deliveryAddress;
    private String phoneNumber;
    private String request;
    private Integer totalAmount;
    private Integer deliveryFee;
    private OrderStatus status;
    private LocalDateTime orderedAt;
    private LocalDateTime completedAt;
    private String cancelReason;
    private boolean hasReview;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()))
                .deliveryAddress(order.getDeliveryAddress())
                .phoneNumber(order.getPhoneNumber())
                .request(order.getRequest())
                .totalAmount(order.getTotalAmount())
                .deliveryFee(order.getDeliveryFee())
                .status(order.getStatus())
                .orderedAt(order.getOrderedAt())
                .completedAt(order.getCompletedAt())
                .cancelReason(order.getCancelReason())
                .hasReview(order.hasReview())
                .build();
    }
}