package com.portfolio.food_delivery.domain.cart.service;

import com.portfolio.food_delivery.domain.cart.dto.*;
import com.portfolio.food_delivery.domain.cart.entity.Cart;
import com.portfolio.food_delivery.domain.cart.entity.CartItem;
import com.portfolio.food_delivery.domain.cart.exception.*;
import com.portfolio.food_delivery.domain.cart.repository.CartRepository;
import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.entity.MenuStatus;
import com.portfolio.food_delivery.domain.menu.exception.MenuNotFoundException;
import com.portfolio.food_delivery.domain.menu.repository.MenuRepository;
import com.portfolio.food_delivery.domain.order.dto.OrderCreateRequest;
import com.portfolio.food_delivery.domain.order.dto.OrderItemRequest;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.exception.UserNotFoundException;
import com.portfolio.food_delivery.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public CartResponse addToCart(Long userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));

        // 메뉴 상태 확인
        if (menu.getStatus() != MenuStatus.AVAILABLE) {
            throw new InvalidCartException("품절된 메뉴는 장바구니에 담을 수 없습니다.");
        }

        // 기존 장바구니 조회 또는 생성
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });

        // 장바구니에 아이템 추가
        CartItem cartItem = CartItem.builder()
                .menu(menu)
                .quantity(request.getQuantity())
                .build();

        cart.addItem(cartItem);

        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse updateCartItem(Long userId, Long menuId, CartUpdateRequest request) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new CartNotFoundException("장바구니를 찾을 수 없습니다."));

        // 권한 확인
        if (!cart.isOwnedBy(userId)) {
            throw new InvalidCartException("자신의 장바구니만 수정할 수 있습니다.");
        }

        // 메뉴가 장바구니에 있는지 확인
        if (cart.findCartItemByMenuId(menuId) == null) {
            throw new CartItemNotFoundException("장바구니에 해당 메뉴가 없습니다.");
        }

        cart.updateItemQuantity(menuId, request.getQuantity());

        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long menuId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new CartNotFoundException("장바구니를 찾을 수 없습니다."));

        // 권한 확인
        if (!cart.isOwnedBy(userId)) {
            throw new InvalidCartException("자신의 장바구니만 수정할 수 있습니다.");
        }

        cart.removeItem(menuId);

        return CartResponse.from(cart);
    }

    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> {
                    // 장바구니가 없으면 빈 장바구니 생성
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
                    return Cart.builder()
                            .id(0L)
                            .user(user)
                            .build();
                });

        return CartResponse.from(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("장바구니를 찾을 수 없습니다."));

        cart.clear();
        log.info("사용자 {}의 장바구니를 비웠습니다.", userId);
    }

    public OrderCreateRequest convertToOrderRequest(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new CartNotFoundException("장바구니를 찾을 수 없습니다."));

        if (cart.isEmpty()) {
            throw new InvalidCartException("장바구니가 비어있습니다.");
        }

        // 장바구니 아이템을 주문 아이템으로 변환
        List<OrderItemRequest> orderItems = cart.getCartItems().stream()
                .map(cartItem -> OrderItemRequest.builder()
                        .menuId(cartItem.getMenu().getId())
                        .quantity(cartItem.getQuantity())
                        .build())
                .collect(Collectors.toList());

        User user = cart.getUser();

        return OrderCreateRequest.builder()
                .restaurantId(cart.getRestaurant().getId())
                .orderItems(orderItems)
                .deliveryAddress(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    public Integer getCartItemCount(Long userId) {
        return cartRepository.countItemsByUserId(userId);
    }

    @Transactional
    public void validateCartItems(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new CartNotFoundException("장바구니를 찾을 수 없습니다."));

        // 모든 메뉴의 상태 재확인
        cart.getCartItems().forEach(item -> {
            Menu menu = item.getMenu();
            if (menu.getStatus() != MenuStatus.AVAILABLE) {
                cart.removeItem(menu.getId());
                log.warn("품절된 메뉴 {}를 장바구니에서 제거했습니다.", menu.getName());
            }
        });
    }
}