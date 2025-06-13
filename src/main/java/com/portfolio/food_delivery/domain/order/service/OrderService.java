package com.portfolio.food_delivery.domain.order.service;

import com.portfolio.food_delivery.domain.cart.service.CartService;
import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.entity.MenuStatus;
import com.portfolio.food_delivery.domain.menu.exception.MenuNotFoundException;
import com.portfolio.food_delivery.domain.menu.repository.MenuRepository;
import com.portfolio.food_delivery.domain.order.dto.OrderCreateRequest;
import com.portfolio.food_delivery.domain.order.dto.OrderItemRequest;
import com.portfolio.food_delivery.domain.order.dto.OrderResponse;
import com.portfolio.food_delivery.domain.order.entity.Order;
import com.portfolio.food_delivery.domain.order.entity.OrderItem;
import com.portfolio.food_delivery.domain.order.entity.OrderStatus;
import com.portfolio.food_delivery.domain.order.exception.InvalidOrderException;
import com.portfolio.food_delivery.domain.order.exception.OrderNotFoundException;
import com.portfolio.food_delivery.domain.order.repository.OrderRepository;
import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.exception.UnauthorizedException;
import com.portfolio.food_delivery.domain.user.entity.User;
import com.portfolio.food_delivery.domain.user.exception.UserNotFoundException;
import com.portfolio.food_delivery.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final CartService cartService;

    @Transactional
    public OrderResponse createOrder(Long userId, OrderCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 주문 항목 생성 및 검증
        List<OrderItem> orderItems = new ArrayList<>();
        Restaurant restaurant = null;
        int totalAmount = 0;

        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            Menu menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다."));

            // 레스토랑 일치 확인
            if (restaurant == null) {
                restaurant = menu.getRestaurant();
            } else if (!restaurant.getId().equals(menu.getRestaurant().getId())) {
                throw new InvalidOrderException("다른 레스토랑의 메뉴를 함께 주문할 수 없습니다.");
            }

            // 메뉴 상태 확인
            if (menu.getStatus() != MenuStatus.AVAILABLE) {
                throw new InvalidOrderException("품절된 메뉴가 포함되어 있습니다: " + menu.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .menu(menu)
                    .quantity(itemRequest.getQuantity())
                    .price(menu.getPrice())
                    .build();

            orderItems.add(orderItem);
            totalAmount += orderItem.getSubtotal();
        }

        // 최소 주문 금액 확인
        if (totalAmount < restaurant.getMinimumOrderAmount()) {
            throw new InvalidOrderException(
                    "최소 주문 금액을 충족하지 않습니다. 최소 주문 금액: " + restaurant.getMinimumOrderAmount() + "원");
        }

        // 주문 생성
        Order order = Order.builder()
                .user(user)
                .restaurant(restaurant)
                .deliveryAddress(request.getDeliveryAddress())
                .phoneNumber(request.getPhoneNumber())
                .request(request.getRequest())
                .totalAmount(totalAmount)
                .deliveryFee(restaurant.getDeliveryFee())
                .orderedAt(LocalDateTime.now())
                .build();

        // 주문 항목 추가
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    @Transactional
    public OrderResponse createOrderFromCart(Long userId) {
        // 장바구니 검증
        cartService.validateCartItems(userId);

        // 장바구니를 주문 요청으로 변환
        OrderCreateRequest request = cartService.convertToOrderRequest(userId);

        // 주문 생성
        OrderResponse orderResponse = createOrder(userId, request);

        // 주문 성공 시 장바구니 비우기
        cartService.clearCart(userId);

        return orderResponse;
    }

    public OrderResponse getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        // 권한 확인 (주문자 또는 레스토랑 오너)
        if (!order.isOwnedBy(userId) && !order.isRestaurantOwnedBy(userId)) {
            throw new UnauthorizedException("주문 조회 권한이 없습니다.");
        }

        return OrderResponse.from(order);
    }

    public Page<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdOrderByOrderedAtDesc(userId, pageable);
        return orders.map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Long userId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        // 권한 확인 (레스토랑 오너만 상태 변경 가능)
        if (!order.isRestaurantOwnedBy(userId)) {
            throw new UnauthorizedException("주문 상태 변경 권한이 없습니다.");
        }

        order.updateStatus(newStatus);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        // 권한 확인 (주문자만 취소 가능)
        if (!order.isOwnedBy(userId)) {
            throw new UnauthorizedException("주문 취소 권한이 없습니다.");
        }

        // 취소 가능 여부 확인
        if (!order.canCancel()) {
            throw new InvalidOrderException("이미 조리가 시작되어 취소할 수 없습니다.");
        }

        order.cancel("사용자 요청");
        return OrderResponse.from(order);
    }
}