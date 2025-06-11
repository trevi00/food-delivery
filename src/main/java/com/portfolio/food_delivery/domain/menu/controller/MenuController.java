package com.portfolio.food_delivery.domain.menu.controller;

import com.portfolio.food_delivery.domain.menu.dto.MenuCreateRequest;
import com.portfolio.food_delivery.domain.menu.dto.MenuResponse;
import com.portfolio.food_delivery.domain.menu.dto.MenuUpdateRequest;
import com.portfolio.food_delivery.domain.menu.entity.MenuStatus;
import com.portfolio.food_delivery.domain.menu.service.MenuService;
import com.portfolio.food_delivery.domain.user.service.UserService;
import com.portfolio.food_delivery.infrastructure.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final UserService userService;

    @PostMapping("/api/restaurants/{restaurantId}/menus")
    public ResponseEntity<MenuResponse> createMenu(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuCreateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        MenuResponse response = menuService.createMenu(restaurantId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/restaurants/{restaurantId}/menus")
    public ResponseEntity<List<MenuResponse>> getMenusByRestaurant(@PathVariable Long restaurantId) {
        List<MenuResponse> responses = menuService.getMenusByRestaurant(restaurantId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/api/menus/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuUpdateRequest request) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        MenuResponse response = menuService.updateMenu(menuId, userId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/menus/{menuId}/status")
    public ResponseEntity<MenuResponse> updateMenuStatus(
            @PathVariable Long menuId,
            @RequestParam MenuStatus status) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        MenuResponse response = menuService.updateMenuStatus(menuId, userId, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long menuId) {
        String email = SecurityUtil.getCurrentUserEmailOrThrow();
        Long userId = userService.getUserIdByEmail(email);

        menuService.deleteMenu(menuId, userId);
        return ResponseEntity.noContent().build();
    }
}