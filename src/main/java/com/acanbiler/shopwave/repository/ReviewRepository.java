package com.acanbiler.shopwave.repository;

import com.acanbiler.shopwave.entity.Review;
import com.acanbiler.shopwave.entity.Product;
import com.acanbiler.shopwave.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Review entity operations.
 * 
 * This repository provides basic CRUD operations and custom query methods
 * for review management, moderation, and analytics.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Basic finder methods
    List<Review> findByProduct(Product product);
    
    Page<Review> findByProduct(Product product, Pageable pageable);
    
    List<Review> findByUser(User user);
    
    Page<Review> findByUser(User user, Pageable pageable);
    
    Optional<Review> findByUserAndProduct(User user, Product product);
    
    boolean existsByUserAndProduct(User user, Product product);

    // Status-based queries
    List<Review> findByStatus(Review.ReviewStatus status);
    
    Page<Review> findByStatus(Review.ReviewStatus status, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.product = :product AND r.status = 'APPROVED'")
    Page<Review> findApprovedReviewsByProduct(@Param("product") Product product, Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.status = :status")
    long countByStatus(@Param("status") Review.ReviewStatus status);

    // Rating-based queries
    List<Review> findByRating(int rating);
    
    Page<Review> findByRating(int rating, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.product = :product AND r.rating >= :minRating AND r.status = 'APPROVED'")
    Page<Review> findByProductAndMinRating(@Param("product") Product product, 
                                         @Param("minRating") int minRating, 
                                         Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.rating >= :minRating AND r.status = 'APPROVED'")
    Page<Review> findByMinRating(@Param("minRating") int minRating, Pageable pageable);

    // Time-based queries
    List<Review> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT r FROM Review r WHERE r.createdAt >= :date AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    Page<Review> findRecentApprovedReviews(@Param("date") LocalDateTime date, Pageable pageable);

    // Verified purchase queries
    @Query("SELECT r FROM Review r WHERE r.verifiedPurchase = true AND r.status = 'APPROVED'")
    Page<Review> findVerifiedPurchaseReviews(Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.product = :product AND r.verifiedPurchase = true AND r.status = 'APPROVED'")
    Page<Review> findVerifiedReviewsByProduct(@Param("product") Product product, Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.verifiedPurchase = true")
    long countVerifiedPurchaseReviews();

    // Helpful reviews queries
    @Query("SELECT r FROM Review r WHERE r.helpfulCount >= :minHelpful AND r.status = 'APPROVED' ORDER BY r.helpfulCount DESC")
    Page<Review> findMostHelpfulReviews(@Param("minHelpful") int minHelpful, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.product = :product AND r.helpfulCount >= :minHelpful AND r.status = 'APPROVED' ORDER BY r.helpfulCount DESC")
    Page<Review> findMostHelpfulReviewsByProduct(@Param("product") Product product, 
                                               @Param("minHelpful") int minHelpful, 
                                               Pageable pageable);

    // Reported reviews queries
    @Query("SELECT r FROM Review r WHERE r.reportedCount > 0 ORDER BY r.reportedCount DESC")
    Page<Review> findReportedReviews(Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.reportedCount >= :threshold")
    List<Review> findHighlyReportedReviews(@Param("threshold") int threshold);

    // Search queries
    @Query("SELECT r FROM Review r WHERE " +
           "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
           "r.status = 'APPROVED'")
    Page<Review> searchReviews(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE " +
           "r.product = :product AND " +
           "(LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "r.status = 'APPROVED'")
    Page<Review> searchReviewsByProduct(@Param("product") Product product, 
                                      @Param("keyword") String keyword, 
                                      Pageable pageable);

    // Statistics queries
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingDistribution();
    
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product = :product AND r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingDistributionByProduct(@Param("product") Product product);
    
    @Query("SELECT " +
           "COUNT(r) as totalReviews, " +
           "COUNT(CASE WHEN r.status = 'APPROVED' THEN 1 END) as approvedReviews, " +
           "COUNT(CASE WHEN r.status = 'PENDING' THEN 1 END) as pendingReviews, " +
           "COUNT(CASE WHEN r.status = 'REJECTED' THEN 1 END) as rejectedReviews, " +
           "COUNT(CASE WHEN r.verifiedPurchase = true THEN 1 END) as verifiedReviews, " +
           "AVG(r.rating) as averageRating " +
           "FROM Review r")
    Object getReviewStatistics();
    
    @Query("SELECT " +
           "AVG(r.rating) as averageRating, " +
           "COUNT(r) as totalReviews, " +
           "COUNT(CASE WHEN r.rating = 5 THEN 1 END) as fiveStarReviews, " +
           "COUNT(CASE WHEN r.rating = 4 THEN 1 END) as fourStarReviews, " +
           "COUNT(CASE WHEN r.rating = 3 THEN 1 END) as threeStarReviews, " +
           "COUNT(CASE WHEN r.rating = 2 THEN 1 END) as twoStarReviews, " +
           "COUNT(CASE WHEN r.rating = 1 THEN 1 END) as oneStarReviews " +
           "FROM Review r WHERE r.product = :product AND r.status = 'APPROVED'")
    Object getProductReviewStatistics(@Param("product") Product product);

    // Top reviewers
    @Query("SELECT r.user, COUNT(r) as reviewCount, AVG(r.rating) as avgRating " +
           "FROM Review r " +
           "WHERE r.status = 'APPROVED' " +
           "GROUP BY r.user " +
           "HAVING COUNT(r) >= :minReviews " +
           "ORDER BY reviewCount DESC")
    Page<Object[]> findTopReviewers(@Param("minReviews") long minReviews, Pageable pageable);

    // Monthly review statistics
    @Query("SELECT " +
           "EXTRACT(YEAR FROM r.createdAt) as year, " +
           "EXTRACT(MONTH FROM r.createdAt) as month, " +
           "COUNT(r) as count, " +
           "AVG(r.rating) as avgRating " +
           "FROM Review r " +
           "WHERE r.createdAt >= :startDate AND r.status = 'APPROVED' " +
           "GROUP BY EXTRACT(YEAR FROM r.createdAt), EXTRACT(MONTH FROM r.createdAt) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyReviewStats(@Param("startDate") LocalDateTime startDate);

    // Product review summaries
    @Query("SELECT r.product, COUNT(r), AVG(r.rating) " +
           "FROM Review r " +
           "WHERE r.status = 'APPROVED' " +
           "GROUP BY r.product " +
           "HAVING COUNT(r) >= :minReviews " +
           "ORDER BY AVG(r.rating) DESC")
    Page<Object[]> findProductReviewSummaries(@Param("minReviews") long minReviews, Pageable pageable);

    // Update operations
    @Modifying
    @Query("UPDATE Review r SET r.status = :status WHERE r.id = :id")
    int updateReviewStatus(@Param("id") Long id, @Param("status") Review.ReviewStatus status);
    
    @Modifying
    @Query("UPDATE Review r SET r.helpfulCount = r.helpfulCount + 1 WHERE r.id = :id")
    int incrementHelpfulCount(@Param("id") Long id);
    
    @Modifying
    @Query("UPDATE Review r SET r.helpfulCount = r.helpfulCount - 1 WHERE r.id = :id AND r.helpfulCount > 0")
    int decrementHelpfulCount(@Param("id") Long id);
    
    @Modifying
    @Query("UPDATE Review r SET r.reportedCount = r.reportedCount + 1 WHERE r.id = :id")
    int incrementReportedCount(@Param("id") Long id);

    // Bulk operations
    @Modifying
    @Query("UPDATE Review r SET r.status = 'APPROVED' WHERE r.status = 'PENDING' AND r.reportedCount = 0")
    int bulkApproveCleanReviews();
    
    @Modifying
    @Query("UPDATE Review r SET r.status = 'FLAGGED' WHERE r.reportedCount >= :threshold")
    int bulkFlagHighlyReportedReviews(@Param("threshold") int threshold);

    // Recent activity
    @Query("SELECT r FROM Review r WHERE r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    Page<Review> findRecentReviews(Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    Page<Review> findPendingReviewsOldestFirst(Pageable pageable);

    // User review history
    @Query("SELECT r FROM Review r WHERE r.user = :user ORDER BY r.createdAt DESC")
    Page<Review> findUserReviewHistory(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.user = :user")
    long countReviewsByUser(@Param("user") User user);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.user = :user")
    Double getAverageRatingByUser(@Param("user") User user);
}