package com.portfolio.food_delivery.domain.menu.entity;

import com.portfolio.food_delivery.common.entity.BaseEntity;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer price;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MenuStatus status = MenuStatus.AVAILABLE;

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    public void update(String name, String description, Integer price, String imageUrl) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }

    public void updateStatus(MenuStatus status) {
        this.status = status;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void delete() {
        this.status = MenuStatus.DELETED;
    }

    public boolean isOwnedBy(Long userId) {
        return this.restaurant.isOwnedBy(userId);
    }
}