package com.portfolio.food_delivery.domain.order.dto;

import com.portfolio.food_delivery.common.entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

    @NotNull(message = "레스토랑 ID는 필수입니다.")
    private Long restaurantId;

    @NotEmpty(message = "주문 항목은 필수입니다.")
    private List<OrderItemRequest> orderItems;

    @NotNull(message = "배달 주소는 필수입니다.")
    private Address deliveryAddress;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phoneNumber;

    private String request;
}