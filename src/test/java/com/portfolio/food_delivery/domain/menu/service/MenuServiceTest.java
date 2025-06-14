package com.portfolio.food_delivery.domain.menu.service;

import com.portfolio.food_delivery.domain.menu.dto.MenuCreateRequest;
import com.portfolio.food_delivery.domain.menu.dto.MenuResponse;
import com.portfolio.food_delivery.domain.menu.dto.MenuUpdateRequest;
import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.entity.MenuStatus;
import com.portfolio.food_delivery.domain.menu.exception.MenuNotFoundException;
import com.portfolio.food_delivery.domain.menu.repository.MenuRepository;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantStatus;
import com.portfolio.food_delivery.domain.restaurant.exception.UnauthorizedException;
import com.portfolio.food_delivery.domain.restaurant.repository.RestaurantRepository;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    @DisplayName("메뉴 등록 성공")
    void createMenu_Success() {
        // given
        Long restaurantId = 1L;
        Long ownerId = 1L;

        User owner = createOwner(ownerId);
        Restaurant restaurant = createRestaurant(owner);

        MenuCreateRequest request = MenuCreateRequest.builder()
                .name("양념치킨")
                .description("특제 양념 소스를 사용한 치킨")
                .price(20000)
                .imageUrl("https://example.com/yangnyeom.jpg")
                .build();

        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        given(menuRepository.countByRestaurantId(restaurantId)).willReturn(0);
        given(menuRepository.save(any(Menu.class))).willAnswer(invocation -> {
            Menu menu = invocation.getArgument(0);
            return Menu.builder()
                    .id(1L)
                    .restaurant(menu.getRestaurant())
                    .name(menu.getName())
                    .description(menu.getDescription())
                    .price(menu.getPrice())
                    .imageUrl(menu.getImageUrl())
                    .status(MenuStatus.AVAILABLE)
                    .displayOrder(1)
                    .build();
        });

        // when
        MenuResponse response = menuService.createMenu(restaurantId, ownerId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("양념치킨");
        assertThat(response.getPrice()).isEqualTo(20000);
        assertThat(response.getStatus()).isEqualTo(MenuStatus.AVAILABLE);

        verify(restaurantRepository).findById(restaurantId);
        verify(menuRepository).countByRestaurantId(restaurantId);
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 등록 실패 - 권한 없음")
    void createMenu_Unauthorized_ThrowsException() {
        // given
        Long restaurantId = 1L;
        Long wrongOwnerId = 2L;

        User owner = createOwner(1L);
        Restaurant restaurant = createRestaurant(owner);

        MenuCreateRequest request = MenuCreateRequest.builder()
                .name("양념치킨")
                .price(20000)
                .build();

        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));

        // when & then
        assertThatThrownBy(() -> menuService.createMenu(restaurantId, wrongOwnerId, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("메뉴 등록 권한이 없습니다.");

        verify(restaurantRepository).findById(restaurantId);
    }

    @Test
    @DisplayName("레스토랑의 메뉴 목록 조회")
    void getMenusByRestaurant_Success() {
        // given
        Long restaurantId = 1L;
        User owner = createOwner(1L);
        Restaurant restaurant = createRestaurant(owner);

        Menu menu1 = Menu.builder()
                .id(1L)
                .restaurant(restaurant)
                .name("양념치킨")
                .price(20000)
                .status(MenuStatus.AVAILABLE)
                .displayOrder(1)
                .build();

        Menu menu2 = Menu.builder()
                .id(2L)
                .restaurant(restaurant)
                .name("후라이드치킨")
                .price(18000)
                .status(MenuStatus.AVAILABLE)
                .displayOrder(2)
                .build();

        given(restaurantRepository.existsById(restaurantId)).willReturn(true);
        given(menuRepository.findByRestaurantIdAndStatusOrderByDisplayOrder(restaurantId, MenuStatus.AVAILABLE))
                .willReturn(Arrays.asList(menu1, menu2));

        // when
        List<MenuResponse> responses = menuService.getMenusByRestaurant(restaurantId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("양념치킨");
        assertThat(responses.get(1).getName()).isEqualTo("후라이드치킨");

        verify(restaurantRepository).existsById(restaurantId);
        verify(menuRepository).findByRestaurantIdAndStatusOrderByDisplayOrder(restaurantId, MenuStatus.AVAILABLE);
    }

    @Test
    @DisplayName("메뉴 수정 성공")
    void updateMenu_Success() {
        // given
        Long menuId = 1L;
        Long ownerId = 1L;

        User owner = createOwner(ownerId);
        Restaurant restaurant = createRestaurant(owner);
        Menu menu = Menu.builder()
                .id(menuId)
                .restaurant(restaurant)
                .name("양념치킨")
                .price(20000)
                .status(MenuStatus.AVAILABLE)
                .build();

        MenuUpdateRequest request = MenuUpdateRequest.builder()
                .name("특제양념치킨")
                .price(22000)
                .description("더욱 맛있어진 양념치킨")
                .build();

        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

        // when
        MenuResponse response = menuService.updateMenu(menuId, ownerId, request);

        // then
        assertThat(response.getName()).isEqualTo("특제양념치킨");
        assertThat(response.getPrice()).isEqualTo(22000);
        assertThat(response.getDescription()).isEqualTo("더욱 맛있어진 양념치킨");

        verify(menuRepository).findById(menuId);
    }

    @Test
    @DisplayName("메뉴 상태 변경 - 품절 처리")
    void updateMenuStatus_ToSoldOut_Success() {
        // given
        Long menuId = 1L;
        Long ownerId = 1L;

        User owner = createOwner(ownerId);
        Restaurant restaurant = createRestaurant(owner);
        Menu menu = Menu.builder()
                .id(menuId)
                .restaurant(restaurant)
                .name("양념치킨")
                .status(MenuStatus.AVAILABLE)
                .build();

        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

        // when
        MenuResponse response = menuService.updateMenuStatus(menuId, ownerId, MenuStatus.SOLD_OUT);

        // then
        assertThat(response.getStatus()).isEqualTo(MenuStatus.SOLD_OUT);

        verify(menuRepository).findById(menuId);
    }

    @Test
    @DisplayName("메뉴 삭제 성공")
    void deleteMenu_Success() {
        // given
        Long menuId = 1L;
        Long ownerId = 1L;

        User owner = createOwner(ownerId);
        Restaurant restaurant = createRestaurant(owner);
        Menu menu = Menu.builder()
                .id(menuId)
                .restaurant(restaurant)
                .name("양념치킨")
                .status(MenuStatus.AVAILABLE)
                .build();

        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

        // when
        menuService.deleteMenu(menuId, ownerId);

        // then
        assertThat(menu.getStatus()).isEqualTo(MenuStatus.DELETED);
        verify(menuRepository).findById(menuId);
    }

    private User createOwner(Long id) {
        return User.builder()
                .id(id)
                .email("owner" + id + "@example.com")
                .role(UserRole.RESTAURANT_OWNER)
                .build();
    }

    private Restaurant createRestaurant(User owner) {
        return Restaurant.builder()
                .id(1L)
                .owner(owner)
                .name("맛있는 치킨")
                .category(RestaurantCategory.CHICKEN)
                .status(RestaurantStatus.OPEN)
                .minimumOrderAmount(15000)
                .build();
    }
}