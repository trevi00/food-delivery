package com.portfolio.food_delivery.domain.payment.controller;

import com.portfolio.food_delivery.domain.payment.dto.*;
import com.portfolio.food_delivery.domain.payment.service.PaymentService;
import com.portfolio.food_delivery.domain.user.service.UserService;
import com.portfolio.food_delivery.infrastructure.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        // 주문자 본인 확인은 PaymentService에서 Order 조회 시 검증
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentCancelRequest request) {
        PaymentResponse response = paymentService.cancelPayment(paymentId, request.getCancelReason());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<PaymentHistoryResponse>> getPaymentHistory(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        Page<PaymentHistoryResponse> response = paymentService.getPaymentHistory(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/date-range")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        List<PaymentHistoryResponse> response = paymentService.getPaymentHistoryByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/status/check")
    public ResponseEntity<Void> checkPaymentStatus(@PathVariable Long paymentId) {
        paymentService.checkAndUpdatePaymentStatus(paymentId);
        return ResponseEntity.ok().build();
    }
}