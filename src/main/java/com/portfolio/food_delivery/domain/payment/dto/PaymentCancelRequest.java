package com.portfolio.food_delivery.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentCancelRequest {

    @NotBlank(message = "취소 사유는 필수입니다.")
    private String cancelReason;
}