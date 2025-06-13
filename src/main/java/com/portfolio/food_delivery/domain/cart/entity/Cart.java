package com.portfolio.food_delivery.domain.cart.entity;

import com.portfolio.food_delivery.common.entity.BaseEntity;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    public void addItem(CartItem item) {
        // 다른 레스토랑의 메뉴를 추가하려는 경우 기존 장바구니 비우기
        if (this.restaurant != null && !this.restaurant.equals(item.getMenu().getRestaurant())) {
            this.clear();
        }

        // 레스토랑 설정
        this.restaurant = item.getMenu().getRestaurant();

        // 동일한 메뉴가 있는지 확인
        CartItem existingItem = findCartItemByMenuId(item.getMenu().getId());
        if (existingItem != null) {
            existingItem.updateQuantity(existingItem.getQuantity() + item.getQuantity());
        } else {
            cartItems.add(item);
            item.setCart(this);
        }
    }

    public void removeItem(Long menuId) {
        cartItems.removeIf(item -> item.getMenu().getId().equals(menuId));

        // 장바구니가 비어있으면 레스토랑 정보도 제거
        if (cartItems.isEmpty()) {
            this.restaurant = null;
        }
    }

    public void updateItemQuantity(Long menuId, Integer quantity) {
        CartItem item = findCartItemByMenuId(menuId);
        if (item != null) {
            if (quantity <= 0) {
                removeItem(menuId);
            } else {
                item.updateQuantity(quantity);
            }
        }
    }

    public void clear() {
        cartItems.clear();
        this.restaurant = null;
    }

    public Integer getTotalAmount() {
        return cartItems.stream()
                .mapToInt(CartItem::getSubtotal)
                .sum();
    }

    public Integer getTotalQuantity() {
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public boolean hasItemsFromRestaurant(Long restaurantId) {
        return this.restaurant != null && this.restaurant.getId().equals(restaurantId);
    }

    public CartItem findCartItemByMenuId(Long menuId) {
        return cartItems.stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst()
                .orElse(null);
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }
}