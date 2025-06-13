// CartItem.java
package com.portfolio.food_delivery.domain.cart.entity;

import com.portfolio.food_delivery.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @Setter
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private Integer quantity;

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSubtotal() {
        return menu.getPrice() * quantity;
    }
}