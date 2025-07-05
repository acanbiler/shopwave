package com.acanbiler.shopwave.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Notification entity representing notifications in the ShopWave application.
 * 
 * This entity supports various notification types and delivery channels
 * with comprehensive tracking and status management.
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user", columnList = "user_id"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_status", columnList = "status"),
    @Index(name = "idx_notification_priority", columnList = "priority"),
    @Index(name = "idx_notification_created", columnList = "created_at"),
    @Index(name = "idx_notification_scheduled", columnList = "scheduled_at"),
    @Index(name = "idx_notification_read", columnList = "read_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user"})
@ToString(exclude = {"user"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    @Column(nullable = false, length = 1000)
    private String message;

    @NotNull(message = "Delivery channel is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_channel", nullable = false, length = 20)
    private DeliveryChannel deliveryChannel;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Size(max = 200, message = "Recipient email must not exceed 200 characters")
    @Email(message = "Recipient email should be valid")
    @Column(name = "recipient_email", length = 200)
    private String recipientEmail;

    @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Size(max = 500, message = "Error message must not exceed 500 characters")
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Size(max = 500, message = "Action URL must not exceed 500 characters")
    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Size(max = 100, message = "Action text must not exceed 100 characters")
    @Column(name = "action_text", length = 100)
    private String actionText;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notification_user"))
    private User user;

    // Utility methods
    public boolean isPending() {
        return NotificationStatus.PENDING.equals(status);
    }

    public boolean isSent() {
        return NotificationStatus.SENT.equals(status);
    }

    public boolean isDelivered() {
        return NotificationStatus.DELIVERED.equals(status);
    }

    public boolean isFailed() {
        return NotificationStatus.FAILED.equals(status);
    }

    public boolean isRead() {
        return readAt != null;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean canRetry() {
        return isFailed() && 
               retryCount != null && 
               maxRetries != null && 
               retryCount < maxRetries;
    }

    public boolean isScheduled() {
        return scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now());
    }

    public boolean isReadyToSend() {
        return isPending() && 
               (scheduledAt == null || scheduledAt.isBefore(LocalDateTime.now())) &&
               !isExpired();
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
        
        if (canRetry()) {
            // Schedule next retry with exponential backoff
            long delayMinutes = (long) Math.pow(2, retryCount) * 5; // 5, 10, 20 minutes
            this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
            this.status = NotificationStatus.PENDING;
        }
    }

    public String getShortMessage() {
        if (message == null || message.length() <= 100) {
            return message;
        }
        return message.substring(0, 97) + "...";
    }

    /**
     * Notification type enumeration.
     */
    public enum NotificationType {
        ORDER_CONFIRMATION("Order confirmation notification"),
        PAYMENT_SUCCESS("Payment successful notification"),
        PAYMENT_FAILED("Payment failed notification"),
        SHIPPING_UPDATE("Shipping status update"),
        DELIVERY_CONFIRMATION("Delivery confirmation"),
        PRODUCT_RESTOCK("Product restock notification"),
        PROMOTION("Promotional notification"),
        REVIEW_REQUEST("Review request notification"),
        ACCOUNT_UPDATE("Account update notification"),
        SECURITY_ALERT("Security alert notification"),
        SYSTEM_MAINTENANCE("System maintenance notification"),
        WELCOME("Welcome notification"),
        PASSWORD_RESET("Password reset notification"),
        EMAIL_VERIFICATION("Email verification notification");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Delivery channel enumeration.
     */
    public enum DeliveryChannel {
        EMAIL("Email notification"),
        SMS("SMS notification"),
        PUSH("Push notification"),
        IN_APP("In-app notification");

        private final String description;

        DeliveryChannel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Notification priority enumeration.
     */
    public enum NotificationPriority {
        LOW("Low priority"),
        NORMAL("Normal priority"),
        HIGH("High priority"),
        URGENT("Urgent priority");

        private final String description;

        NotificationPriority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Notification status enumeration.
     */
    public enum NotificationStatus {
        PENDING("Notification is pending to be sent"),
        SENT("Notification has been sent"),
        DELIVERED("Notification has been delivered"),
        FAILED("Notification delivery failed");

        private final String description;

        NotificationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}