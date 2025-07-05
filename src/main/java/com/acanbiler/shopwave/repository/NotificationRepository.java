package com.acanbiler.shopwave.repository;

import com.acanbiler.shopwave.entity.Notification;
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

/**
 * Repository interface for Notification entity operations.
 * 
 * This repository provides basic CRUD operations and custom query methods
 * for notification management, delivery tracking, and analytics.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // User-based queries
    List<Notification> findByUser(User user);
    
    Page<Notification> findByUser(User user, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    Page<Notification> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user")
    long countByUser(@Param("user") User user);

    // Status-based queries
    List<Notification> findByStatus(Notification.NotificationStatus status);
    
    Page<Notification> findByStatus(Notification.NotificationStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status")
    long countByStatus(@Param("status") Notification.NotificationStatus status);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.status = :status")
    Page<Notification> findByUserAndStatus(@Param("user") User user, 
                                         @Param("status") Notification.NotificationStatus status, 
                                         Pageable pageable);

    // Type-based queries
    List<Notification> findByType(Notification.NotificationType type);
    
    Page<Notification> findByType(Notification.NotificationType type, Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.type = :type")
    long countByType(@Param("type") Notification.NotificationType type);

    // Priority-based queries
    List<Notification> findByPriority(Notification.NotificationPriority priority);
    
    Page<Notification> findByPriority(Notification.NotificationPriority priority, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.priority IN ('HIGH', 'URGENT') AND n.status = 'PENDING' ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findHighPriorityPendingNotifications();

    // Delivery channel queries
    List<Notification> findByDeliveryChannel(Notification.DeliveryChannel deliveryChannel);
    
    Page<Notification> findByDeliveryChannel(Notification.DeliveryChannel deliveryChannel, Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.deliveryChannel = :channel")
    long countByDeliveryChannel(@Param("channel") Notification.DeliveryChannel channel);

    // Read status queries
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.readAt IS NULL")
    List<Notification> findUnreadByUser(@Param("user") User user);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.readAt IS NULL")
    Page<Notification> findUnreadByUser(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.readAt IS NULL")
    long countUnreadByUser(@Param("user") User user);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.readAt IS NOT NULL")
    Page<Notification> findReadByUser(@Param("user") User user, Pageable pageable);

    // Pending notifications for processing
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND " +
           "(n.scheduledAt IS NULL OR n.scheduledAt <= :now) AND " +
           "(n.expiresAt IS NULL OR n.expiresAt > :now) " +
           "ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findNotificationsReadyToSend(@Param("now") LocalDateTime now);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.scheduledAt <= :now ORDER BY n.scheduledAt ASC")
    List<Notification> findScheduledNotificationsDue(@Param("now") LocalDateTime now);

    // Failed notifications for retry
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND " +
           "n.retryCount < n.maxRetries AND " +
           "(n.nextRetryAt IS NULL OR n.nextRetryAt <= :now)")
    List<Notification> findNotificationsForRetry(@Param("now") LocalDateTime now);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount >= n.maxRetries")
    List<Notification> findPermanentlyFailedNotifications();

    // Time-based queries
    List<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT n FROM Notification n WHERE n.sentAt BETWEEN :startDate AND :endDate")
    List<Notification> findBySentAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :date ORDER BY n.createdAt DESC")
    Page<Notification> findRecentNotifications(@Param("date") LocalDateTime date, Pageable pageable);

    // Expired notifications
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= :now AND n.status = 'PENDING'")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= :now")
    long countExpiredNotifications(@Param("now") LocalDateTime now);

    // Search queries
    @Query("SELECT n FROM Notification n WHERE " +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.message) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Notification> searchNotifications(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.message) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Notification> searchNotificationsByUser(@Param("user") User user, 
                                                @Param("keyword") String keyword, 
                                                Pageable pageable);

    // Statistics queries
    @Query("SELECT " +
           "COUNT(n) as totalNotifications, " +
           "COUNT(CASE WHEN n.status = 'SENT' THEN 1 END) as sentNotifications, " +
           "COUNT(CASE WHEN n.status = 'DELIVERED' THEN 1 END) as deliveredNotifications, " +
           "COUNT(CASE WHEN n.status = 'FAILED' THEN 1 END) as failedNotifications, " +
           "COUNT(CASE WHEN n.status = 'PENDING' THEN 1 END) as pendingNotifications, " +
           "COUNT(CASE WHEN n.readAt IS NOT NULL THEN 1 END) as readNotifications " +
           "FROM Notification n")
    Object getNotificationStatistics();
    
    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type ORDER BY COUNT(n) DESC")
    List<Object[]> getNotificationCountByType();
    
    @Query("SELECT n.deliveryChannel, COUNT(n), " +
           "COUNT(CASE WHEN n.status = 'DELIVERED' THEN 1 END) as delivered, " +
           "COUNT(CASE WHEN n.status = 'FAILED' THEN 1 END) as failed " +
           "FROM Notification n " +
           "GROUP BY n.deliveryChannel")
    List<Object[]> getDeliveryChannelStats();

    // Daily notification statistics
    @Query("SELECT " +
           "DATE(n.createdAt) as notificationDate, " +
           "COUNT(n) as dailyCount, " +
           "COUNT(CASE WHEN n.status = 'DELIVERED' THEN 1 END) as dailyDelivered " +
           "FROM Notification n " +
           "WHERE n.createdAt >= :startDate " +
           "GROUP BY DATE(n.createdAt) " +
           "ORDER BY notificationDate")
    List<Object[]> getDailyNotificationStats(@Param("startDate") LocalDateTime startDate);

    // User engagement statistics
    @Query("SELECT n.user, " +
           "COUNT(n) as totalNotifications, " +
           "COUNT(CASE WHEN n.readAt IS NOT NULL THEN 1 END) as readNotifications " +
           "FROM Notification n " +
           "GROUP BY n.user " +
           "HAVING COUNT(n) >= :minNotifications " +
           "ORDER BY COUNT(CASE WHEN n.readAt IS NOT NULL THEN 1 END) DESC")
    Page<Object[]> getUserEngagementStats(@Param("minNotifications") long minNotifications, Pageable pageable);

    // Recent activity
    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    Page<Notification> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'DELIVERED' ORDER BY n.deliveredAt DESC")
    Page<Notification> findRecentDeliveredNotifications(Pageable pageable);

    // Update operations
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.sentAt = :sentAt WHERE n.id = :id")
    int markAsSent(@Param("id") Long id, 
                  @Param("status") Notification.NotificationStatus status, 
                  @Param("sentAt") LocalDateTime sentAt);
    
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.deliveredAt = :deliveredAt WHERE n.id = :id")
    int markAsDelivered(@Param("id") Long id, 
                       @Param("status") Notification.NotificationStatus status, 
                       @Param("deliveredAt") LocalDateTime deliveredAt);
    
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt WHERE n.id = :id")
    int markAsRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);
    
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.failedAt = :failedAt, n.errorMessage = :errorMessage, " +
           "n.retryCount = n.retryCount + 1, n.nextRetryAt = :nextRetryAt WHERE n.id = :id")
    int markAsFailed(@Param("id") Long id, 
                    @Param("status") Notification.NotificationStatus status, 
                    @Param("failedAt") LocalDateTime failedAt, 
                    @Param("errorMessage") String errorMessage, 
                    @Param("nextRetryAt") LocalDateTime nextRetryAt);

    // Bulk operations
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt WHERE n.user = :user AND n.readAt IS NULL")
    int markAllAsReadByUser(@Param("user") User user, @Param("readAt") LocalDateTime readAt);
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= :now")
    int deleteExpiredNotifications(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt <= :cutoffDate AND n.status IN ('DELIVERED', 'FAILED')")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Email-specific queries
    @Query("SELECT n FROM Notification n WHERE n.deliveryChannel = 'EMAIL' AND n.recipientEmail = :email")
    List<Notification> findEmailNotificationsByRecipient(@Param("email") String email);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.deliveryChannel = 'EMAIL' AND n.status = 'FAILED'")
    long countFailedEmailNotifications();

    // Priority and urgency queries
    @Query("SELECT n FROM Notification n WHERE n.priority = 'URGENT' AND n.status = 'PENDING'")
    List<Notification> findUrgentPendingNotifications();
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.priority IN ('HIGH', 'URGENT') AND n.status = 'PENDING'")
    long countHighPriorityPendingNotifications();

    // Notification templates and types
    @Query("SELECT n.type, n.deliveryChannel, COUNT(n) as count, AVG(CASE WHEN n.readAt IS NOT NULL THEN 1.0 ELSE 0.0 END) as readRate " +
           "FROM Notification n " +
           "WHERE n.sentAt >= :startDate " +
           "GROUP BY n.type, n.deliveryChannel " +
           "ORDER BY count DESC")
    List<Object[]> getNotificationPerformanceStats(@Param("startDate") LocalDateTime startDate);
}