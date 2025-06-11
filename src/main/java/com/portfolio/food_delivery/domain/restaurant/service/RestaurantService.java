package com.portfolio.food_delivery.domain.restaurant.service;

import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantCreateRequest;
import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantResponse;
import com.portfolio.food_delivery.domain.restaurant.dto.RestaurantUpdateRequest;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantStatus;
import com.portfolio.food_delivery.domain.restaurant.exception.RestaurantNotFoundException;
import com.portfolio.food_delivery.domain.restaurant.exception.UnauthorizedException;
import com.portfolio.food_delivery.domain.restaurant.repository.RestaurantRepository;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.entity.UserRole;
import com.portfolio.food_delivery.domain.user.exception.UserNotFoundException;
import com.portfolio.food_delivery.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    public RestaurantResponse createRestaurant(Long ownerId, RestaurantCreateRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 권한 확인
        if (!owner.getRole().equals(UserRole.RESTAURANT_OWNER)) {
            throw new UnauthorizedException("레스토랑 등록 권한이 없습니다.");
        }

        Restaurant restaurant = Restaurant.builder()
                .owner(owner)
                .name(request.getName())
                .description(request.getDescription())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .category(request.getCategory())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .minimumOrderAmount(request.getMinimumOrderAmount())
                .deliveryFee(request.getDeliveryFee())
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return RestaurantResponse.from(savedRestaurant);
    }

    public RestaurantResponse getRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("레스토랑을 찾을 수 없습니다."));

        return RestaurantResponse.from(restaurant);
    }

    public Page<RestaurantResponse> getRestaurants(RestaurantStatus status,
                                                   RestaurantCategory category,
                                                   Pageable pageable) {
        Page<Restaurant> restaurants;

        if (category != null) {
            restaurants = restaurantRepository.findByStatusAndCategory(status, category, pageable);
        } else {
            restaurants = restaurantRepository.findByStatus(status, pageable);
        }

        return restaurants.map(RestaurantResponse::from);
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long restaurantId, Long userId,
                                               RestaurantUpdateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("레스토랑을 찾을 수 없습니다."));

        // 소유자 확인
        if (!restaurant.isOwnedBy(userId)) {
            throw new UnauthorizedException("레스토랑 수정 권한이 없습니다.");
        }

        restaurant.update(
                request.getDescription(),
                request.getMinimumOrderAmount(),
                request.getDeliveryFee(),
                request.getOpenTime(),
                request.getCloseTime()
        );

        return RestaurantResponse.from(restaurant);
    }
}