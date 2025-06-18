package com.portfolio.food_delivery.domain.order.dto;

import com.portfolio.food_delivery.common.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

@Schema(description = "주문 생성 요청 정보")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

    @Schema(description = "레스토랑 ID", example = "1", required = true)
    @NotNull(message = "레스토랑 ID는 필수입니다.")
    private Long restaurantId;

    @Schema(description = "주문 항목 목록", required = true)
    @NotEmpty(message = "주문 항목은 필수입니다.")
    private List<OrderItemRequest> orderItems;

    @Schema(description = "배달 주소", required = true)
    @NotNull(message = "배달 주소는 필수입니다.")
    private Address deliveryAddress;

    @Schema(description = "전화번호", example = "010-1234-5678", required = true)
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phoneNumber;

    @Schema(description = "요청사항", example = "문 앞에 놔주세요", required = false)
    private String request;
}