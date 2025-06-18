package com.portfolio.food_delivery.domain.order.dto;

import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "주문 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    @Schema(description = "주문 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "레스토랑 ID", example = "1")
    private Long restaurantId;

    @Schema(description = "레스토랑명", example = "맛있는 치킨")
    private String restaurantName;

    @Schema(description = "주문 항목 목록")
    private List<OrderItemResponse> orderItems;

    @Schema(description = "배달 주소")
    private Address deliveryAddress;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "요청사항", example = "문 앞에 놔주세요")
    private String request;

    @Schema(description = "총 금액 (메뉴 금액만)", example = "25000")
    private Integer totalAmount;

    @Schema(description = "배달료", example = "3000")
    private Integer deliveryFee;

    @Schema(description = "주문 상태", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "주문일시", example = "2025-01-15T12:30:00")
    private LocalDateTime orderedAt;

    @Schema(description = "완료일시", example = "2025-01-15T13:30:00")
    private LocalDateTime completedAt;

    @Schema(description = "취소 사유", example = "고객 요청")
    private String cancelReason;

    @Schema(description = "리뷰 작성 여부", example = "false")
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