package com.portfolio.food_delivery.domain.review.repository;

import com.portfolio.food_delivery.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 레스토랑의 리뷰 조회 (삭제되지 않은 것만)
    Page<Review> findByRestaurantIdAndIsDeletedFalseOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);

    // 사용자의 리뷰 조회
    Page<Review> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 주문에 대한 리뷰 존재 여부 확인
    boolean existsByOrderIdAndIsDeletedFalse(Long orderId);

    // 리뷰 상세 조회 (연관 엔티티 포함)
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.user " +
            "JOIN FETCH r.restaurant " +
            "JOIN FETCH r.order " +
            "WHERE r.id = :id AND r.isDeleted = false")
    Optional<Review> findByIdWithDetails(@Param("id") Long id);

    // 레스토랑의 평균 평점 계산
    @Query("SELECT AVG(r.rating) FROM Review r " +
            "WHERE r.restaurant.id = :restaurantId AND r.isDeleted = false")
    Double calculateAverageRating(@Param("restaurantId") Long restaurantId);

    // 레스토랑의 리뷰 개수
    @Query("SELECT COUNT(r) FROM Review r " +
            "WHERE r.restaurant.id = :restaurantId AND r.isDeleted = false")
    Integer countByRestaurantId(@Param("restaurantId") Long restaurantId);

    // 평점별 리뷰 개수 조회
    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
            "WHERE r.restaurant.id = :restaurantId AND r.isDeleted = false " +
            "GROUP BY r.rating")
    List<Object[]> countByRating(@Param("restaurantId") Long restaurantId);
}