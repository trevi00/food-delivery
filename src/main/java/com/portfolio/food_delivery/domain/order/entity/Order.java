package com.portfolio.food_delivery.domain.order.entity;

import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.common.entity.BaseEntity;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Embedded
    private Address deliveryAddress;

    @Column(nullable = false)
    private String phoneNumber;

    private String request;

    @Column(nullable = false)
    private Integer totalAmount;

    @Column(nullable = false)
    private Integer deliveryFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    private LocalDateTime completedAt;

    private String cancelReason;

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public void cancel(String reason) {
        if (!canCancel()) {
            throw new IllegalStateException("이미 조리가 시작되어 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
        this.completedAt = LocalDateTime.now();
    }

    public boolean canCancel() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    public boolean isRestaurantOwnedBy(Long userId) {
        return this.restaurant.isOwnedBy(userId);
    }
}