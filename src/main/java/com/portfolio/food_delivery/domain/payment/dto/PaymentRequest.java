package com.portfolio.food_delivery.domain.payment.dto;

import com.portfolio.food_delivery.domain.payment.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Schema(description = "결제 요청 정보")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @Schema(description = "주문 ID", example = "1", required = true)
    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;

    @Schema(description = "결제 수단", example = "CREDIT_CARD", required = true,
            allowableValues = {"CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "KAKAO_PAY", "NAVER_PAY", "TOSS"})
    @NotNull(message = "결제 수단은 필수입니다.")
    private PaymentMethod paymentMethod;

    @Schema(description = "카드 번호 (카드 결제 시)", example = "1234567812345678")
    private String cardNumber;

    @Schema(description = "카드 CVC (카드 결제 시)", example = "123")
    private String cardCvc;

    @Schema(description = "카드 유효기간 (MM/YY)", example = "12/25", pattern = "^(0[1-9]|1[0-2])/([0-9]{2})$")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "유효기간은 MM/YY 형식이어야 합니다.")
    private String cardExpiry;

    @Schema(description = "은행 코드 (계좌이체 시)", example = "004")
    private String bankCode;

    @Schema(description = "계좌번호 (계좌이체 시)", example = "123456789012")
    private String accountNumber;

    @Schema(description = "간편결제 토큰 (간편결제 시)", example = "token_123456")
    private String easyPayToken;
}