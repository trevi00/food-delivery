package com.portfolio.food_delivery.domain.cart.controller;

import com.portfolio.food_delivery.domain.cart.dto.CartItemRequest;
import com.portfolio.food_delivery.domain.cart.dto.CartResponse;
import com.portfolio.food_delivery.domain.cart.dto.CartUpdateRequest;
import com.portfolio.food_delivery.domain.cart.service.CartService;
import com.portfolio.food_delivery.domain.order.dto.OrderCreateRequest;
import com.portfolio.food_delivery.domain.order.dto.OrderResponse;
import com.portfolio.food_delivery.domain.order.service.OrderService;
import com.portfolio.food_delivery.domain.user.service.UserService;
import com.portfolio.food_delivery.infrastructure.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody CartItemRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        CartResponse response = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{menuId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long menuId,
            @Valid @RequestBody CartUpdateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        CartResponse response = cartService.updateCartItem(userId, menuId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{menuId}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable Long menuId) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        CartResponse response = cartService.removeFromCart(userId, menuId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount() {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        Integer count = cartService.getCartItemCount(userId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validateCart() {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        cartService.validateCartItems(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/from-cart")
    public ResponseEntity<OrderResponse> createOrderFromCart() {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        OrderResponse response = orderService.createOrderFromCart(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}