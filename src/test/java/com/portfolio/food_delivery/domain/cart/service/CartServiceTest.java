package com.portfolio.food_delivery.domain.cart.service;

import com.portfolio.food_delivery.domain.cart.dto.*;
import com.portfolio.food_delivery.domain.cart.entity.Cart;
import com.portfolio.food_delivery.domain.cart.entity.CartItem;
import com.portfolio.food_delivery.domain.cart.exception.CartItemNotFoundException;
import com.portfolio.food_delivery.domain.cart.exception.InvalidCartException;
import com.portfolio.food_delivery.domain.cart.repository.CartRepository;
import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.entity.MenuStatus;
import com.portfolio.food_delivery.domain.menu.exception.MenuNotFoundException;
import com.portfolio.food_delivery.domain.menu.repository.MenuRepository;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.entity.UserRole;
import com.portfolio.food_delivery.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    @DisplayName("장바구니에 메뉴 추가 성공 - 새로운 장바구니")
    void addToCart_NewCart_Success() {
        // given
        Long userId = 1L;
        Long menuId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Menu menu = createMenu(menuId, restaurant, "양념치킨", 20000);

        CartItemRequest request = CartItemRequest.builder()
                .menuId(menuId)
                .quantity(2)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));
        given(cartRepository.findByUserId(userId)).willReturn(Optional.empty());
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            // ID 설정을 위한 새 Cart 생성
            Cart savedCart = Cart.builder()
                    .id(1L)
                    .user(cart.getUser())
                    .restaurant(cart.getRestaurant())
                    .build();
            // CartItem 복사
            cart.getCartItems().forEach(item -> {
                CartItem newItem = CartItem.builder()
                        .id(1L)
                        .menu(item.getMenu())
                        .quantity(item.getQuantity())
                        .build();
                savedCart.addItem(newItem);
            });
            return savedCart;
        });

        // when
        CartResponse response = cartService.addToCart(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRestaurantId()).isEqualTo(restaurant.getId());
        assertThat(response.getTotalQuantity()).isEqualTo(2);
        assertThat(response.getTotalAmount()).isEqualTo(40000);
        assertThat(response.getItems()).hasSize(1);

        verify(userRepository).findById(userId);
        verify(menuRepository).findById(menuId);
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("장바구니에 메뉴 추가 성공 - 기존 장바구니에 같은 레스토랑 메뉴")
    void addToCart_ExistingCart_SameRestaurant_Success() {
        // given
        Long userId = 1L;
        Long menuId = 2L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Menu existingMenu = createMenu(1L, restaurant, "양념치킨", 20000);
        Menu newMenu = createMenu(menuId, restaurant, "후라이드치킨", 18000);

        Cart existingCart = createCartWithItem(user, restaurant, existingMenu, 1);

        CartItemRequest request = CartItemRequest.builder()
                .menuId(menuId)
                .quantity(1)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(menuRepository.findById(menuId)).willReturn(Optional.of(newMenu));
        given(cartRepository.findByUserId(userId)).willReturn(Optional.of(existingCart));

        // when
        CartResponse response = cartService.addToCart(userId, request);

        // then
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalAmount()).isEqualTo(38000); // 20000 + 18000

        verify(cartRepository, never()).save(any(Cart.class)); // 기존 카트 수정이므로 save 호출 안함
    }

    @Test
    @DisplayName("장바구니에 메뉴 추가 - 다른 레스토랑 메뉴 추가 시 기존 장바구니 초기화")
    void addToCart_DifferentRestaurant_ClearCart() {
        // given
        Long userId = 1L;
        Long menuId = 3L;

        User user = createUser(userId);
        Restaurant restaurant1 = createRestaurant();
        Restaurant restaurant2 = createRestaurant2();
        Menu existingMenu = createMenu(1L, restaurant1, "양념치킨", 20000);
        Menu newMenu = createMenu(menuId, restaurant2, "피자", 25000);

        Cart existingCart = createCartWithItem(user, restaurant1, existingMenu, 2);

        CartItemRequest request = CartItemRequest.builder()
                .menuId(menuId)
                .quantity(1)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(menuRepository.findById(menuId)).willReturn(Optional.of(newMenu));
        given(cartRepository.findByUserId(userId)).willReturn(Optional.of(existingCart));

        // when
        CartResponse response = cartService.addToCart(userId, request);

        // then
        assertThat(response.getRestaurantId()).isEqualTo(restaurant2.getId());
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalAmount()).isEqualTo(25000);
    }

    @Test
    @DisplayName("장바구니에 메뉴 추가 실패 - 품절 메뉴")
    void addToCart_SoldOutMenu_Fail() {
        // given
        Long userId = 1L;
        Long menuId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Menu menu = createMenu(menuId, restaurant, "양념치킨", 20000);
        menu.updateStatus(MenuStatus.SOLD_OUT);

        CartItemRequest request = CartItemRequest.builder()
                .menuId(menuId)
                .quantity(1)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

        // when & then
        assertThatThrownBy(() -> cartService.addToCart(userId, request))
                .isInstanceOf(InvalidCartException.class)
                .hasMessage("품절된 메뉴는 장바구니에 담을 수 없습니다.");
    }

    @Test
    @DisplayName("장바구니 아이템 수량 변경 성공")
    void updateCartItem_Success() {
        // given
        Long userId = 1L;
        Long menuId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Menu menu = createMenu(menuId, restaurant, "양념치킨", 20000);
        Cart cart = createCartWithItem(user, restaurant, menu, 2);

        CartUpdateRequest request = CartUpdateRequest.builder()
                .quantity(3)
                .build();

        given(cartRepository.findByUserIdWithItems(userId)).willReturn(Optional.of(cart));

        // when
        CartResponse response = cartService.updateCartItem(userId, menuId, request);

        // then
        assertThat(response.getTotalQuantity()).isEqualTo(3);
        assertThat(response.getTotalAmount()).isEqualTo(60000);
    }

    @Test
    @DisplayName("장바구니 아이템 수량 0으로 변경 시 삭제")
    void updateCartItem_ZeroQuantity_RemoveItem() {
        // given
        Long userId = 1L;
        Long menuId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Menu menu = createMenu(menuId, restaurant, "양념치킨", 20000);
        Cart cart = createCartWithItem(user, restaurant, menu, 2);

        CartUpdateRequest request = CartUpdateRequest.builder()
                .quantity(0)
                .build();

        given(cartRepository.findByUserIdWithItems(userId)).willReturn(Optional.of(cart));

        // when
        CartResponse response = cartService.updateCartItem(userId, menuId, request);

        // then
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 성공")
    void removeFromCart_Success() {
        // given
        Long userId = 1L;
        Long menuId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Menu menu = createMenu(menuId, restaurant, "양념치킨", 20000);
        Cart cart = createCartWithItem(user, restaurant, menu, 2);

        given(cartRepository.findByUserIdWithItems(userId)).willReturn(Optional.of(cart));

        // when
        CartResponse response = cartService.removeFromCart(userId, menuId);

        // then
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("장바구니 조회 성공")
    void getCart_Success() {
        // given
        Long userId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Menu menu = createMenu(1L, restaurant, "양념치킨", 20000);
        Cart cart = createCartWithItem(user, restaurant, menu, 2);

        given(cartRepository.findByUserIdWithItems(userId)).willReturn(Optional.of(cart));

        // when
        CartResponse response = cartService.getCart(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRestaurantId()).isEqualTo(restaurant.getId());
        assertThat(response.getTotalAmount()).isEqualTo(40000);
        assertThat(response.getCanOrder()).isTrue();
    }

    @Test
    @DisplayName("장바구니 비우기 성공")
    void clearCart_Success() {
        // given
        Long userId = 1L;

        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Menu menu = createMenu(1L, restaurant, "양념치킨", 20000);
        Cart cart = createCartWithItem(user, restaurant, menu, 2);

        given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));

        // when
        cartService.clearCart(userId);

        // then
        assertThat(cart.isEmpty()).isTrue();
        assertThat(cart.getRestaurant()).isNull();
    }

    // Helper methods
    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .role(UserRole.CUSTOMER)
                .build();
    }

    private Restaurant createRestaurant() {
        return Restaurant.builder()
                .id(1L)
                .name("맛있는 치킨")
                .category(RestaurantCategory.CHICKEN)
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();
    }

    private Restaurant createRestaurant2() {
        return Restaurant.builder()
                .id(2L)
                .name("피자하우스")
                .category(RestaurantCategory.PIZZA)
                .minimumOrderAmount(20000)
                .deliveryFee(2000)
                .build();
    }

    private Menu createMenu(Long id, Restaurant restaurant, String name, Integer price) {
        return Menu.builder()
                .id(id)
                .restaurant(restaurant)
                .name(name)
                .price(price)
                .status(MenuStatus.AVAILABLE)
                .build();
    }

    private Cart createCartWithItem(User user, Restaurant restaurant, Menu menu, Integer quantity) {
        Cart cart = Cart.builder()
                .id(1L)
                .user(user)
                .restaurant(restaurant)
                .build();

        CartItem item = CartItem.builder()
                .menu(menu)
                .quantity(quantity)
                .build();

        cart.addItem(item);
        return cart;
    }
}