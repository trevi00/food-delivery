package com.portfolio.food_delivery.domain.payment.dto;

import com.portfolio.food_delivery.domain.payment.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;

    @NotNull(message = "결제 수단은 필수입니다.")
    private PaymentMethod paymentMethod;

    // 카드 결제인 경우
    private String cardNumber; // 실제로는 PG사에서 토큰화된 값 사용

    private String cardCvc;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "유효기간은 MM/YY 형식이어야 합니다.")
    private String cardExpiry;

    // 계좌이체인 경우
    private String bankCode;

    private String accountNumber;

    // 간편결제인 경우
    private String easyPayToken;
}