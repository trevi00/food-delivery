package com.portfolio.food_delivery.domain.review.entity;

import com.portfolio.food_delivery.common.entity.BaseEntity;
import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Integer rating; // 1-5 별점

    @Column(length = 1000)
    private String content;

    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    // 리뷰 답글 (사장님 답변)
    @Column(length = 500)
    private String reply;

    private LocalDateTime repliedAt;

    public void updateContent(String content, Integer rating) {
        this.content = content;
        this.rating = rating;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void addReply(String reply) {
        this.reply = reply;
        this.repliedAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    public boolean isRestaurantOwnedBy(Long userId) {
        return this.restaurant.isOwnedBy(userId);
    }
}