package com.acanbiler.shopwave.repository;

import com.acanbiler.shopwave.entity.Payment;
import com.acanbiler.shopwave.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity operations.
 * 
 * This repository provides basic CRUD operations and custom query methods
 * for payment processing, analytics, and financial reporting.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Basic finder methods
    Optional<Payment> findByReferenceNumber(String referenceNumber);
    
    Optional<Payment> findByProviderPaymentId(String providerPaymentId);
    
    List<Payment> findByUser(User user);
    
    Page<Payment> findByUser(User user, Pageable pageable);
    
    boolean existsByReferenceNumber(String referenceNumber);

    // Status-based queries
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.user = :user AND p.status = :status")
    Page<Payment> findByUserAndStatus(@Param("user") User user, 
                                    @Param("status") Payment.PaymentStatus status, 
                                    Pageable pageable);

    // Provider-based queries
    List<Payment> findByProvider(Payment.PaymentProvider provider);
    
    Page<Payment> findByProvider(Payment.PaymentProvider provider, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.provider = :provider")
    long countByProvider(@Param("provider") Payment.PaymentProvider provider);

    // Amount-based queries
    @Query("SELECT p FROM Payment p WHERE p.amount >= :minAmount AND p.amount <= :maxAmount")
    Page<Payment> findByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                  @Param("maxAmount") BigDecimal maxAmount, 
                                  Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.amount >= :amount AND p.status = 'COMPLETED'")
    List<Payment> findCompletedPaymentsAboveAmount(@Param("amount") BigDecimal amount);

    // Time-based queries
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.processedAt BETWEEN :startDate AND :endDate AND p.status = 'COMPLETED'")
    List<Payment> findCompletedPaymentsBetween(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :date ORDER BY p.createdAt DESC")
    Page<Payment> findRecentPayments(@Param("date") LocalDateTime date, Pageable pageable);

    // Failed payment queries
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.failedAt >= :date")
    List<Payment> findRecentFailedPayments(@Param("date") LocalDateTime date);
    
    @Query("SELECT p FROM Payment p WHERE p.user = :user AND p.status = 'FAILED'")
    List<Payment> findFailedPaymentsByUser(@Param("user") User user);

    // Refund queries
    @Query("SELECT p FROM Payment p WHERE p.refundAmount IS NOT NULL AND p.refundAmount > 0")
    List<Payment> findRefundedPayments();
    
    @Query("SELECT p FROM Payment p WHERE p.refundAmount IS NOT NULL AND p.refundAmount = p.amount")
    List<Payment> findFullyRefundedPayments();
    
    @Query("SELECT p FROM Payment p WHERE p.refundAmount IS NOT NULL AND p.refundAmount > 0 AND p.refundAmount < p.amount")
    List<Payment> findPartiallyRefundedPayments();

    // Dispute queries
    @Query("SELECT p FROM Payment p WHERE p.disputeAmount IS NOT NULL AND p.disputeAmount > 0")
    List<Payment> findDisputedPayments();
    
    @Query("SELECT p FROM Payment p WHERE p.disputedAt >= :date")
    List<Payment> findRecentDisputes(@Param("date") LocalDateTime date);

    // Payment method queries
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :method AND p.status = 'COMPLETED'")
    List<Payment> findCompletedPaymentsByMethod(@Param("method") Payment.PaymentMethod method);
    
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodDistribution();

    // Card-based queries
    @Query("SELECT p FROM Payment p WHERE p.cardLastFour = :lastFour AND p.user = :user")
    List<Payment> findByUserAndCardLastFour(@Param("user") User user, @Param("lastFour") String lastFour);
    
    @Query("SELECT p FROM Payment p WHERE p.cardBrand = :brand AND p.status = 'COMPLETED'")
    List<Payment> findCompletedPaymentsByCardBrand(@Param("brand") String brand);

    // Statistics queries
    @Query("SELECT " +
           "COUNT(p) as totalPayments, " +
           "COUNT(CASE WHEN p.status = 'COMPLETED' THEN 1 END) as completedPayments, " +
           "COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END) as failedPayments, " +
           "COUNT(CASE WHEN p.status = 'PENDING' THEN 1 END) as pendingPayments, " +
           "SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END) as totalRevenue, " +
           "AVG(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE NULL END) as averageAmount " +
           "FROM Payment p")
    Object getPaymentStatistics();
    
    @Query("SELECT " +
           "SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END) as revenue, " +
           "COUNT(CASE WHEN p.status = 'COMPLETED' THEN 1 END) as completedCount, " +
           "COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END) as failedCount " +
           "FROM Payment p " +
           "WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate")
    Object getPaymentStatisticsBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    // Daily revenue statistics
    @Query("SELECT " +
           "DATE(p.processedAt) as paymentDate, " +
           "SUM(p.amount) as dailyRevenue, " +
           "COUNT(p) as dailyCount " +
           "FROM Payment p " +
           "WHERE p.status = 'COMPLETED' AND p.processedAt >= :startDate " +
           "GROUP BY DATE(p.processedAt) " +
           "ORDER BY paymentDate")
    List<Object[]> getDailyRevenueStats(@Param("startDate") LocalDateTime startDate);
    
    // Monthly revenue statistics
    @Query("SELECT " +
           "EXTRACT(YEAR FROM p.processedAt) as year, " +
           "EXTRACT(MONTH FROM p.processedAt) as month, " +
           "SUM(p.amount) as monthlyRevenue, " +
           "COUNT(p) as monthlyCount, " +
           "AVG(p.amount) as averageAmount " +
           "FROM Payment p " +
           "WHERE p.status = 'COMPLETED' AND p.processedAt >= :startDate " +
           "GROUP BY EXTRACT(YEAR FROM p.processedAt), EXTRACT(MONTH FROM p.processedAt) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyRevenueStats(@Param("startDate") LocalDateTime startDate);

    // Provider performance statistics
    @Query("SELECT " +
           "p.provider, " +
           "COUNT(p) as totalTransactions, " +
           "COUNT(CASE WHEN p.status = 'COMPLETED' THEN 1 END) as successfulTransactions, " +
           "COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END) as failedTransactions, " +
           "SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END) as totalRevenue " +
           "FROM Payment p " +
           "GROUP BY p.provider")
    List<Object[]> getProviderPerformanceStats();

    // Top customers by payment volume
    @Query("SELECT " +
           "p.user, " +
           "COUNT(p) as paymentCount, " +
           "SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END) as totalSpent " +
           "FROM Payment p " +
           "WHERE p.status = 'COMPLETED' " +
           "GROUP BY p.user " +
           "HAVING COUNT(p) >= :minPayments " +
           "ORDER BY totalSpent DESC")
    Page<Object[]> findTopCustomersByRevenue(@Param("minPayments") long minPayments, Pageable pageable);

    // Recent transactions
    @Query("SELECT p FROM Payment p ORDER BY p.createdAt DESC")
    Page<Payment> findRecentTransactions(Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' ORDER BY p.processedAt DESC")
    Page<Payment> findRecentCompletedPayments(Pageable pageable);

    // Webhook tracking
    @Query("SELECT p FROM Payment p WHERE p.webhookReceivedAt IS NOT NULL ORDER BY p.webhookReceivedAt DESC")
    Page<Payment> findPaymentsWithWebhooks(Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.webhookReceivedAt IS NULL AND p.status IN ('COMPLETED', 'FAILED')")
    long countPaymentsMissingWebhooks();

    // Update operations
    @Modifying
    @Query("UPDATE Payment p SET p.status = :status, p.processedAt = :processedAt WHERE p.id = :id")
    int updatePaymentStatus(@Param("id") Long id, 
                          @Param("status") Payment.PaymentStatus status, 
                          @Param("processedAt") LocalDateTime processedAt);
    
    @Modifying
    @Query("UPDATE Payment p SET p.status = :status, p.failedAt = :failedAt, p.failureReason = :reason WHERE p.id = :id")
    int updatePaymentFailure(@Param("id") Long id, 
                           @Param("status") Payment.PaymentStatus status, 
                           @Param("failedAt") LocalDateTime failedAt, 
                           @Param("reason") String reason);
    
    @Modifying
    @Query("UPDATE Payment p SET p.webhookData = :webhookData, p.webhookReceivedAt = :receivedAt WHERE p.id = :id")
    int updateWebhookData(@Param("id") Long id, 
                        @Param("webhookData") String webhookData, 
                        @Param("receivedAt") LocalDateTime receivedAt);
    
    @Modifying
    @Query("UPDATE Payment p SET p.refundAmount = :refundAmount, p.refundedAt = :refundedAt WHERE p.id = :id")
    int updateRefundInfo(@Param("id") Long id, 
                       @Param("refundAmount") BigDecimal refundAmount, 
                       @Param("refundedAt") LocalDateTime refundedAt);

    // Currency-based queries
    @Query("SELECT p.currency, SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END) FROM Payment p GROUP BY p.currency")
    List<Object[]> getRevenueByCurrency();
    
    @Query("SELECT p FROM Payment p WHERE p.currency = :currency AND p.status = 'COMPLETED'")
    Page<Payment> findCompletedPaymentsByCurrency(@Param("currency") String currency, Pageable pageable);

    // Fraud detection queries
    @Query("SELECT p FROM Payment p WHERE p.user = :user AND p.status = 'FAILED' AND p.createdAt >= :date")
    List<Payment> findRecentFailedPaymentsByUser(@Param("user") User user, @Param("date") LocalDateTime date);
    
    @Query("SELECT p.user, COUNT(p) FROM Payment p WHERE p.status = 'FAILED' AND p.createdAt >= :date GROUP BY p.user HAVING COUNT(p) >= :threshold")
    List<Object[]> findUsersWithMultipleFailedPayments(@Param("date") LocalDateTime date, @Param("threshold") long threshold);
}