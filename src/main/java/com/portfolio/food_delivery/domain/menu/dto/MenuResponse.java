package com.portfolio.food_delivery.domain.menu.dto;

import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.entity.MenuStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {
    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private Integer price;
    private String imageUrl;
    private MenuStatus status;
    private Integer displayOrder;
    private LocalDateTime createdAt;

    public static MenuResponse from(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .restaurantId(menu.getRestaurant().getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .imageUrl(menu.getImageUrl())
                .status(menu.getStatus())
                .displayOrder(menu.getDisplayOrder())
                .createdAt(menu.getCreatedAt())
                .build();
    }
}