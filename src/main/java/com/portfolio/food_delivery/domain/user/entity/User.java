package com.portfolio.food_delivery.domain.user.entity;

import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.common.entity.BaseEntity;
import com.portfolio.food_delivery.domain.review.entity.Review;
import jakarta.persistence.*;
import lombok.*;
import com.portfolio.food_delivery.domain.order.entity.Order;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateProfile(String name, String phoneNumber, Address address) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}