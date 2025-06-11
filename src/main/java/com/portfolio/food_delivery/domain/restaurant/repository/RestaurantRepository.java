package com.portfolio.food_delivery.domain.restaurant.repository;

import com.portfolio.food_delivery.domain.restaurant.entity.Restaurant;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantCategory;
import com.portfolio.food_delivery.domain.restaurant.entity.RestaurantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Page<Restaurant> findByStatus(RestaurantStatus status, Pageable pageable);

    Page<Restaurant> findByStatusAndCategory(RestaurantStatus status,
                                             RestaurantCategory category,
                                             Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE r.status = :status " +
            "AND r.name LIKE %:keyword% OR r.description LIKE %:keyword%")
    Page<Restaurant> searchByKeyword(@Param("status") RestaurantStatus status,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    List<Restaurant> findByOwnerId(Long ownerId);
}