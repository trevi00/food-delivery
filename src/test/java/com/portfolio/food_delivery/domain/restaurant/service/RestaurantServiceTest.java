package com.portfolio.food_delivery.domain.restaurant.service;

import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantCreateRequest;
import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantResponse;
import com.portfolio.food_delivery.domain.restaurant.exception.UnauthorizedException;
import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantUpdateRequest;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantStatus;
import com.portfolio.food_delivery.domain.restaurant.exception.RestaurantNotFoundException;
import com.portfolio.food_delivery.domain.restaurant.exception.UnauthorizedException;  // 이 줄 추가!
import com.portfolio.food_delivery.domain.restaurant.repository.RestaurantRepository;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.entity.UserRole;
import com.portfolio.food_delivery.domain.user.exception.UserNotFoundException;
import com.portfolio.food_delivery.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    @Test
    @DisplayName("레스토랑 등록 성공")
    void createRestaurant_Success() {
        // given
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .email("owner@example.com")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        RestaurantCreateRequest request = RestaurantCreateRequest.builder()
                .name("맛있는 치킨")
                .description("바삭한 치킨 전문점")
                .phoneNumber("02-1234-5678")
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .category(RestaurantCategory.CHICKEN)
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(22, 0))
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();

        given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(restaurantRepository.save(any(Restaurant.class))).willAnswer(invocation -> {
            Restaurant restaurant = invocation.getArgument(0);
            return Restaurant.builder()
                    .id(1L)
                    .owner(restaurant.getOwner())
                    .name(restaurant.getName())
                    .description(restaurant.getDescription())
                    .phoneNumber(restaurant.getPhoneNumber())
                    .address(restaurant.getAddress())
                    .category(restaurant.getCategory())
                    .openTime(restaurant.getOpenTime())
                    .closeTime(restaurant.getCloseTime())
                    .minimumOrderAmount(restaurant.getMinimumOrderAmount())
                    .deliveryFee(restaurant.getDeliveryFee())
                    .rating(0.0)
                    .reviewCount(0)
                    .status(RestaurantStatus.OPEN)
                    .build();
        });

        // when
        RestaurantResponse response = restaurantService.createRestaurant(ownerId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("맛있는 치킨");
        assertThat(response.getCategory()).isEqualTo(RestaurantCategory.CHICKEN);
        assertThat(response.getStatus()).isEqualTo(RestaurantStatus.OPEN);
        assertThat(response.getRating()).isEqualTo(0.0);

        verify(userRepository).findById(ownerId);
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("레스토랑 등록 실패 - 권한 없는 사용자")
    void createRestaurant_UnauthorizedUser_ThrowsException() {
        // given
        Long userId = 1L;
        User customer = User.builder()
                .id(userId)
                .email("customer@example.com")
                .role(UserRole.CUSTOMER)  // RESTAURANT_OWNER가 아님
                .build();

        RestaurantCreateRequest request = RestaurantCreateRequest.builder()
                .name("맛있는 치킨")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(customer));

        // when & then
        assertThatThrownBy(() -> restaurantService.createRestaurant(userId, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("레스토랑 등록 권한이 없습니다.");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("레스토랑 조회 성공")
    void getRestaurant_Success() {
        // given
        Long restaurantId = 1L;
        Restaurant restaurant = createTestRestaurant();

        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));

        // when
        RestaurantResponse response = restaurantService.getRestaurant(restaurantId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(restaurantId);
        assertThat(response.getName()).isEqualTo("맛있는 치킨");

        verify(restaurantRepository).findById(restaurantId);
    }

    @Test
    @DisplayName("레스토랑 목록 조회 - 페이징")
    void getRestaurants_WithPaging_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        User owner1 = User.builder()
                .id(1L)
                .email("owner1@example.com")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        User owner2 = User.builder()
                .id(2L)
                .email("owner2@example.com")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        Restaurant restaurant1 = Restaurant.builder()
                .id(1L)
                .owner(owner1)
                .name("맛있는 치킨")
                .description("바삭한 치킨 전문점")
                .phoneNumber("02-1234-5678")
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .category(RestaurantCategory.CHICKEN)
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(22, 0))
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .rating(4.2)
                .reviewCount(120)
                .status(RestaurantStatus.OPEN)
                .build();

        Restaurant restaurant2 = Restaurant.builder()
                .id(2L)
                .owner(owner2)
                .name("피자하우스")
                .description("화덕 피자 전문점")
                .phoneNumber("02-2345-6789")
                .address(new Address("서울시", "강남구", "선릉로", "456", "12346"))
                .category(RestaurantCategory.PIZZA)
                .openTime(LocalTime.of(11, 0))
                .closeTime(LocalTime.of(23, 0))
                .minimumOrderAmount(20000)
                .deliveryFee(2000)
                .rating(4.5)
                .reviewCount(200)
                .status(RestaurantStatus.OPEN)
                .build();

        Page<Restaurant> restaurantPage = new PageImpl<>(
                Arrays.asList(restaurant1, restaurant2),
                pageable,
                2
        );

        given(restaurantRepository.findByStatus(RestaurantStatus.OPEN, pageable))
                .willReturn(restaurantPage);

        // when
        Page<RestaurantResponse> response = restaurantService.getRestaurants(
                RestaurantStatus.OPEN, null, pageable);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent().get(0).getName()).isEqualTo("맛있는 치킨");
        assertThat(response.getContent().get(1).getName()).isEqualTo("피자하우스");
    }

    @Test
    @DisplayName("레스토랑 정보 수정 성공")
    void updateRestaurant_Success() {
        // given
        Long restaurantId = 1L;
        Long ownerId = 1L;

        Restaurant restaurant = createTestRestaurant();
        RestaurantUpdateRequest request = RestaurantUpdateRequest.builder()
                .description("더욱 맛있는 치킨 전문점")
                .minimumOrderAmount(20000)
                .deliveryFee(2000)
                .build();

        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));

        // when
        RestaurantResponse response = restaurantService.updateRestaurant(
                restaurantId, ownerId, request);

        // then
        assertThat(response.getDescription()).isEqualTo("더욱 맛있는 치킨 전문점");
        assertThat(response.getMinimumOrderAmount()).isEqualTo(20000);
        assertThat(response.getDeliveryFee()).isEqualTo(2000);

        verify(restaurantRepository).findById(restaurantId);
    }

    private Restaurant createTestRestaurant() {
        User owner = User.builder()
                .id(1L)
                .email("owner@example.com")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        return Restaurant.builder()
                .id(1L)
                .owner(owner)
                .name("맛있는 치킨")
                .description("바삭한 치킨 전문점")
                .phoneNumber("02-1234-5678")
                .address(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .category(RestaurantCategory.CHICKEN)
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(22, 0))
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .rating(4.2)
                .reviewCount(120)
                .status(RestaurantStatus.OPEN)
                .build();
    }
}