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
import com.portfolio.food_delivery.presentation.advice.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "장바구니 관련 API")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService;

    @Operation(summary = "장바구니에 메뉴 추가", description = "선택한 메뉴를 장바구니에 추가합니다. 다른 레스토랑의 메뉴 추가 시 기존 장바구니는 초기화됩니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "장바구니 추가 성공",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (품절 메뉴 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @Parameter(description = "장바구니 추가 정보", required = true)
            @Valid @RequestBody CartItemRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        CartResponse response = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "장바구니 조회", description = "현재 사용자의 장바구니를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "장바구니 아이템 수량 변경", description = "장바구니에 담긴 메뉴의 수량을 변경합니다. 수량이 0이면 장바구니에서 제거됩니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "장바구니 아이템을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/items/{menuId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @Parameter(description = "메뉴 ID", required = true, example = "1")
            @PathVariable Long menuId,
            @Parameter(description = "수량 변경 정보", required = true)
            @Valid @RequestBody CartUpdateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        CartResponse response = cartService.updateCartItem(userId, menuId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "장바구니 아이템 삭제", description = "장바구니에서 특정 메뉴를 제거합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "장바구니 아이템을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/items/{menuId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @Parameter(description = "메뉴 ID", required = true, example = "1")
            @PathVariable Long menuId) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        CartResponse response = cartService.removeFromCart(userId, menuId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "장바구니 비우기", description = "장바구니의 모든 아이템을 삭제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "장바구니를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "장바구니 아이템 개수 조회", description = "장바구니에 담긴 메뉴의 종류 수를 반환합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Integer.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount() {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        Integer count = cartService.getCartItemCount(userId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "장바구니 유효성 검증", description = "장바구니에 담긴 메뉴들의 현재 상태를 확인하고 품절된 메뉴를 제거합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검증 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "장바구니를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/validate")
    public ResponseEntity<Void> validateCart() {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        cartService.validateCartItems(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "장바구니에서 주문 생성",
            description = "현재 장바구니의 내용으로 주문을 생성합니다. 주문 성공 시 장바구니는 자동으로 비워집니다.",
            deprecated = true)
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "주문 생성 성공",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 장바구니, 최소 주문 금액 미달 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/from-cart")
    public ResponseEntity<OrderResponse> createOrderFromCart() {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        OrderResponse response = orderService.createOrderFromCart(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}