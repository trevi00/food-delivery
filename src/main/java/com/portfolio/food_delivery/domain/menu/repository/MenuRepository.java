package com.portfolio.food_delivery.domain.menu.repository;

import com.portfolio.food_delivery.domain.menu.entity.Menu;
import com.portfolio.food_delivery.domain.menu.entity.MenuStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByRestaurantIdAndStatusOrderByDisplayOrder(Long restaurantId, MenuStatus status);

    List<Menu> findByRestaurantIdOrderByDisplayOrder(Long restaurantId);

    @Query("SELECT m FROM Menu m JOIN FETCH m.restaurant WHERE m.id = :id")
    Optional<Menu> findByIdWithRestaurant(@Param("id") Long id);

    Integer countByRestaurantId(Long restaurantId);
}