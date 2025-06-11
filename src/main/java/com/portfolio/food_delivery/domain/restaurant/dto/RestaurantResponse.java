package com.portfolio.food_delivery.domain.restaurant.dto;

import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponse {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private String phoneNumber;
    private Address address;
    private RestaurantCategory category;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer minimumOrderAmount;
    private Integer deliveryFee;
    private Double rating;
    private Integer reviewCount;
    private RestaurantStatus status;
    private LocalDateTime createdAt;

    public static RestaurantResponse from(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .ownerId(restaurant.getOwner().getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .phoneNumber(restaurant.getPhoneNumber())
                .address(restaurant.getAddress())
                .category(restaurant.getCategory())
                .openTime(restaurant.getOpenTime())
                .closeTime(restaurant.getCloseTime())
                .minimumOrderAmount(restaurant.getMinimumOrderAmount())
                .deliveryFee(restaurant.getDeliveryFee())
                .rating(restaurant.getRating())
                .reviewCount(restaurant.getReviewCount())
                .status(restaurant.getStatus())
                .createdAt(restaurant.getCreatedAt())
                .build();
    }
}