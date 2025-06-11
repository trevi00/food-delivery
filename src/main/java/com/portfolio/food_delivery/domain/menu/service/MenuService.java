package com.portfolio.food_delivery.domain.menu.service;

import com.portfolio.food_delivery.domain.menu.dto.MenuCreateRequest;
import com.portfolio.food_delivery.domain.menu.dto.MenuResponse;
import com.portfolio.food_delivery.domain.menu.dto.MenuUpdateRequest;
import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.entity.MenuStatus;
import com.portfolio.food_delivery.domain.menu.exception.MenuNotFoundException;
import com.portfolio.food_delivery.domain.menu.repository.MenuRepository;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.exception.RestaurantNotFoundException;
import com.portfolio.food_delivery.domain.restaurant.exception.UnauthorizedException;
import com.portfolio.food_delivery.domain.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public MenuResponse createMenu(Long restaurantId, Long userId, MenuCreateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("레스토랑을 찾을 수 없습니다."));

        // 권한 확인
        if (!restaurant.isOwnedBy(userId)) {
            throw new UnauthorizedException("메뉴 등록 권한이 없습니다.");
        }

        // 표시 순서 계산
        Integer displayOrder = menuRepository.countByRestaurantId(restaurantId) + 1;

        Menu menu = Menu.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .displayOrder(displayOrder)
                .build();

        Menu savedMenu = menuRepository.save(menu);
        return MenuResponse.from(savedMenu);
    }

    public List<MenuResponse> getMenusByRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException("레스토랑을 찾을 수 없습니다.");
        }

        List<Menu> menus = menuRepository.findByRestaurantIdAndStatusOrderByDisplayOrder(
                restaurantId, MenuStatus.AVAILABLE);

        return menus.stream()
                .map(MenuResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuResponse updateMenu(Long menuId, Long userId, MenuUpdateRequest request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));

        // 권한 확인
        if (!menu.isOwnedBy(userId)) {
            throw new UnauthorizedException("메뉴 수정 권한이 없습니다.");
        }

        menu.update(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getImageUrl()
        );

        return MenuResponse.from(menu);
    }

    @Transactional
    public MenuResponse updateMenuStatus(Long menuId, Long userId, MenuStatus status) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));

        // 권한 확인
        if (!menu.isOwnedBy(userId)) {
            throw new UnauthorizedException("메뉴 상태 변경 권한이 없습니다.");
        }

        menu.updateStatus(status);
        return MenuResponse.from(menu);
    }

    @Transactional
    public void deleteMenu(Long menuId, Long userId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));

        // 권한 확인
        if (!menu.isOwnedBy(userId)) {
            throw new UnauthorizedException("메뉴 삭제 권한이 없습니다.");
        }

        // 소프트 삭제
        menu.delete();
    }
}