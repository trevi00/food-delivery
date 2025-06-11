package com.portfolio.food_delivery.domain.restaurant.controller;

import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantCreateRequest;
import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantResponse;
import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantUpdateRequest;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantStatus;
import com.portfolio.food_delivery.domain.restaurant.service.RestaurantService;
import com.portfolio.food_delivery.domain.user.service.UserService;
import com.portfolio.food_delivery.infrastructure.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Valid @RequestBody RestaurantCreateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        log.debug("Creating restaurant for user: {}", email);
        Long userId = userService.getUserIdByEmail(email);

        RestaurantResponse response = restaurantService.createRestaurant(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurant(@PathVariable Long id) {
        RestaurantResponse response = restaurantService.getRestaurant(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantResponse>> getRestaurants(
            @RequestParam(defaultValue = "OPEN") RestaurantStatus status,
            @RequestParam(required = false) RestaurantCategory category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<RestaurantResponse> response = restaurantService.getRestaurants(status, category, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantUpdateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        log.debug("Updating restaurant {} for user: {}", id, email);
        Long userId = userService.getUserIdByEmail(email);

        RestaurantResponse response = restaurantService.updateRestaurant(id, userId, request);
        return ResponseEntity.ok(response);
    }
}