package com.portfolio.food_delivery.domain.order.service;

import com.portfolio.food_delivery.common.entity.Address;
import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.entity.MenuStatus;
import com.portfolio.food_delivery.domain.menu.repository.MenuRepository;
import com.portfolio.food_delivery.domain.order.dto.*;
import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.order.entity.OrderItem;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import com.portfolio.food_delivery.domain.order.exception.InvalidOrderException;
import com.portfolio.food_delivery.domain.order.exception.OrderNotFoundException;
import com.portfolio.food_delivery.domain.order.repository.OrderRepository;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantStatus;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        // given
        Long userId = 1L;
        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Menu menu1 = createMenu(1L, restaurant, "양념치킨", 20000);
        Menu menu2 = createMenu(2L, restaurant, "콜라", 2000);

        OrderCreateRequest request = OrderCreateRequest.builder()
                .restaurantId(restaurant.getId())
                .orderItems(Arrays.asList(
                        OrderItemRequest.builder().menuId(1L).quantity(1).build(),
                        OrderItemRequest.builder().menuId(2L).quantity(2).build()
                ))
                .deliveryAddress(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .phoneNumber("010-1234-5678")
                .request("문 앞에 놔주세요")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu1));
        given(menuRepository.findById(2L)).willReturn(Optional.of(menu2));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return Order.builder()
                    .id(1L)
                    .user(order.getUser())
                    .restaurant(order.getRestaurant())
                    .orderItems(order.getOrderItems())
                    .deliveryAddress(order.getDeliveryAddress())
                    .phoneNumber(order.getPhoneNumber())
                    .request(order.getRequest())
                    .totalAmount(order.getTotalAmount())
                    .deliveryFee(order.getDeliveryFee())
                    .status(OrderStatus.PENDING)
                    .orderedAt(LocalDateTime.now())
                    .build();
        });

        // when
        OrderResponse response = orderService.createOrder(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTotalAmount()).isEqualTo(24000); // 20000 + 2000*2
        assertThat(response.getDeliveryFee()).isEqualTo(3000);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getOrderItems()).hasSize(2);

        verify(userRepository).findById(userId);
        verify(menuRepository).findById(1L);
        verify(menuRepository).findById(2L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 생성 실패 - 최소 주문 금액 미달")
    void createOrder_MinimumOrderAmountFail() {
        // given
        Long userId = 1L;
        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Menu menu = createMenu(1L, restaurant, "콜라", 2000);

        OrderCreateRequest request = OrderCreateRequest.builder()
                .restaurantId(restaurant.getId())
                .orderItems(Arrays.asList(
                        OrderItemRequest.builder().menuId(1L).quantity(1).build()
                ))
                .deliveryAddress(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("최소 주문 금액을 충족하지 않습니다. 최소 주문 금액: 15000원");

        verify(userRepository).findById(userId);
        verify(menuRepository).findById(1L);
    }

    @Test
    @DisplayName("주문 생성 실패 - 품절 메뉴 포함")
    void createOrder_SoldOutMenuFail() {
        // given
        Long userId = 1L;
        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Menu menu = createMenu(1L, restaurant, "양념치킨", 20000);
        menu.updateStatus(MenuStatus.SOLD_OUT);

        OrderCreateRequest request = OrderCreateRequest.builder()
                .restaurantId(restaurant.getId())
                .orderItems(Arrays.asList(
                        OrderItemRequest.builder().menuId(1L).quantity(1).build()
                ))
                .deliveryAddress(new Address("서울시", "강남구", "테헤란로", "123", "12345"))
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("품절된 메뉴가 포함되어 있습니다: 양념치킨");
    }

    @Test
    @DisplayName("주문 상태 변경 - 레스토랑 오너")
    void updateOrderStatus_ByOwner_Success() {
        // given
        Long orderId = 1L;
        Long ownerId = 1L;
        User owner = createOwner(ownerId);
        Restaurant restaurant = createRestaurantWithOwner(owner);
        Order order = createOrder(orderId, createUser(2L), restaurant);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when
        OrderResponse response = orderService.updateOrderStatus(orderId, ownerId, OrderStatus.PREPARING);

        // then
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PREPARING);
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("사용자 주문 내역 조회")
    void getMyOrders_Success() {
        // given
        Long userId = 1L;
        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Order order1 = createOrder(1L, user, restaurant);
        Order order2 = createOrder(2L, user, restaurant);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(order1, order2), pageable, 2);

        given(orderRepository.findByUserIdOrderByOrderedAtDesc(userId, pageable)).willReturn(orderPage);

        // when
        Page<OrderResponse> response = orderService.getMyOrders(userId, pageable);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);

        verify(orderRepository).findByUserIdOrderByOrderedAtDesc(userId, pageable);
    }

    @Test
    @DisplayName("주문 취소 성공 - 주문 직후")
    void cancelOrder_Success() {
        // given
        Long orderId = 1L;
        Long userId = 1L;
        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Order order = createOrder(orderId, user, restaurant);
        order.updateStatus(OrderStatus.PENDING);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when
        OrderResponse response = orderService.cancelOrder(orderId, userId);

        // then
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("주문 취소 실패 - 이미 조리 시작")
    void cancelOrder_AlreadyPreparing_Fail() {
        // given
        Long orderId = 1L;
        Long userId = 1L;
        User user = createUser(userId);
        Restaurant restaurant = createRestaurant();
        Order order = createOrder(orderId, user, restaurant);
        order.updateStatus(OrderStatus.PREPARING);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("이미 조리가 시작되어 취소할 수 없습니다.");
    }

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .role(UserRole.CUSTOMER)
                .build();
    }

    private User createOwner(Long id) {
        return User.builder()
                .id(id)
                .email("owner" + id + "@example.com")
                .role(UserRole.RESTAURANT_OWNER)
                .build();
    }

    private Restaurant createRestaurant() {
        return Restaurant.builder()
                .id(1L)
                .owner(createOwner(1L))
                .name("맛있는 치킨")
                .category(RestaurantCategory.CHICKEN)
                .status(RestaurantStatus.OPEN)
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();
    }

    private Restaurant createRestaurantWithOwner(User owner) {
        return Restaurant.builder()
                .id(1L)
                .owner(owner)
                .name("맛있는 치킨")
                .category(RestaurantCategory.CHICKEN)
                .status(RestaurantStatus.OPEN)
                .minimumOrderAmount(15000)
                .deliveryFee(3000)
                .build();
    }

    private Menu createMenu(Long id, Restaurant restaurant, String name, Integer price) {
        return Menu.builder()
                .id(id)
                .restaurant(restaurant)
                .name(name)
                .price(price)
                .status(MenuStatus.AVAILABLE)
                .build();
    }

    private Order createOrder(Long id, User user, Restaurant restaurant) {
        return Order.builder()
                .id(id)
                .user(user)
                .restaurant(restaurant)
                .orderItems(Arrays.asList())
                .totalAmount(20000)
                .deliveryFee(3000)
                .status(OrderStatus.PENDING)
                .orderedAt(LocalDateTime.now())
                .build();
    }
}