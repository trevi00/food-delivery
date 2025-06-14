package com.portfolio.food_delivery.domain.payment.repository;

import com.portfolio.food_delivery.domain.payment.entity.Payment;
import com.portfolio.food_delivery.domain.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 주문별 결제 정보 조회
    Optional<Payment> findByOrderId(Long orderId);

    // 거래 ID로 결제 조회
    Optional<Payment> findByTransactionId(String transactionId);

    // 사용자의 결제 내역 조회
    @Query("SELECT p FROM Payment p JOIN p.order o WHERE o.user.id = :userId ORDER BY p.createdAt DESC")
    Page<Payment> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // 특정 상태의 결제 조회
    List<Payment> findByStatus(PaymentStatus status);

    // 기간별 결제 내역 조회
    @Query("SELECT p FROM Payment p JOIN p.order o " +
            "WHERE o.user.id = :userId " +
            "AND p.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 결제 금액 합계
    @Query("SELECT SUM(p.amount) FROM Payment p " +
            "WHERE p.status = :status " +
            "AND p.paidAt BETWEEN :startDate AND :endDate")
    Integer sumAmountByStatusAndDateRange(
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 주문에 대한 결제 존재 여부 확인
    boolean existsByOrderId(Long orderId);
}