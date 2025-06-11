package com.portfolio.food_delivery.domain.restaurant.entity;

import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.common.entity.BaseEntity;
import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Restaurant extends BaseEntity {

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Menu> menus = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String phoneNumber;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantCategory category;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

    @Column(nullable = false)
    private Integer minimumOrderAmount;

    @Column(nullable = false)
    private Integer deliveryFee;

    @Column(nullable = false)
    @Builder.Default
    private Double rating = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RestaurantStatus status = RestaurantStatus.OPEN;

    public void update(String description, Integer minimumOrderAmount,
                       Integer deliveryFee, LocalTime openTime, LocalTime closeTime) {
        if (description != null) this.description = description;
        if (minimumOrderAmount != null) this.minimumOrderAmount = minimumOrderAmount;
        if (deliveryFee != null) this.deliveryFee = deliveryFee;
        if (openTime != null) this.openTime = openTime;
        if (closeTime != null) this.closeTime = closeTime;
    }

    public void updateStatus(RestaurantStatus status) {
        this.status = status;
    }

    public boolean isOwnedBy(Long userId) {
        return this.owner.getId().equals(userId);
    }

    public void updateRating(Double newRating, Integer newReviewCount) {
        this.rating = newRating;
        this.reviewCount = newReviewCount;
    }
}