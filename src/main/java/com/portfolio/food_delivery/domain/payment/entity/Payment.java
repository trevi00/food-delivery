package com.portfolio.food_delivery.domain.payment.entity;

import com.portfolio.food_delivery.common.entity.BaseEntity;
import com.portfolio.food_delivery.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private Integer amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method; // 결제 수단

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING; // 결제 상태

    @Column(unique = true)
    private String transactionId; // PG사 거래 ID

    private String cardNumber; // 마스킹된 카드번호 (예: **** **** **** 1234)

    private LocalDateTime paidAt; // 결제 완료 시간

    private LocalDateTime cancelledAt; // 취소 시간

    private String cancelReason; // 취소 사유

    private String failureReason; // 실패 사유

    // 결제 처리 시작
    public void startProcessing() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("대기 중인 결제만 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.PROCESSING;
    }

    // 결제 성공 처리
    public void completePayment(String transactionId, String maskedCardNumber) {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("처리 중인 결제만 완료할 수 있습니다.");
        }
        this.status = PaymentStatus.SUCCESS;
        this.transactionId = transactionId;
        this.cardNumber = maskedCardNumber;
        this.paidAt = LocalDateTime.now();
    }

    // 결제 실패 처리
    public void failPayment(String failureReason) {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("처리 중인 결제만 실패 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    // 결제 취소
    public void cancelPayment(String cancelReason) {
        if (this.status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("성공한 결제만 취소할 수 있습니다.");
        }
        this.status = PaymentStatus.CANCELLED;
        this.cancelReason = cancelReason;
        this.cancelledAt = LocalDateTime.now();
    }

    // 부분 취소 (추후 구현)
    public void partialCancel(Integer cancelAmount, String cancelReason) {
        if (this.status != PaymentStatus.SUCCESS && this.status != PaymentStatus.PARTIAL_CANCELLED) {
            throw new IllegalStateException("성공한 결제만 부분 취소할 수 있습니다.");
        }
        // TODO: 부분 취소 로직 구현
        this.status = PaymentStatus.PARTIAL_CANCELLED;
        this.cancelReason = cancelReason;
    }

    public boolean isSuccess() {
        return this.status == PaymentStatus.SUCCESS;
    }

    public boolean isCancellable() {
        return this.status == PaymentStatus.SUCCESS;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }
}