package com.portfolio.food_delivery.domain.order.controller;

import com.portfolio.food_delivery.domain.order.dto.OrderCreateRequest;
import com.portfolio.food_delivery.domain.order.dto.OrderResponse;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import com.portfolio.food_delivery.domain.order.service.OrderService;
import com.portfolio.food_delivery.domain.user.service.UserService;
import com.portfolio.food_delivery.infrastructure.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        OrderResponse response = orderService.getOrder(orderId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @PageableDefault(size = 20, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        Page<OrderResponse> response = orderService.getMyOrders(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        OrderResponse response = orderService.updateOrderStatus(orderId, userId, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        OrderResponse response = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(response);
    }
}