package com.portfolio.food_delivery.domain.cart.repository;

import com.portfolio.food_delivery.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    @Query("SELECT c FROM Cart c " +
            "LEFT JOIN FETCH c.cartItems ci " +
            "LEFT JOIN FETCH ci.menu m " +
            "LEFT JOIN FETCH c.restaurant r " +
            "WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.user.id = :userId")
    Integer countItemsByUserId(@Param("userId") Long userId);
}
