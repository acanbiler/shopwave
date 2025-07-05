package com.acanbiler.shopwave.controller;

import com.acanbiler.shopwave.entity.Notification;
import com.acanbiler.shopwave.entity.User;
import com.acanbiler.shopwave.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification management controller for ShopWave application.
 * 
 * This controller handles notification operations, user notifications,
 * bulk messaging, and notification analytics with proper authorization controls.
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Management", description = "User notification and messaging operations")
@SecurityRequirement(name = "Bearer Authentication")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get all notifications (admin only).
     */
    @Operation(
        summary = "Get all notifications",
        description = "Retrieves paginated list of all notifications. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationPageResponse> getAllNotifications(
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Notification> notifications = notificationService.findAll(pageable);
        NotificationPageResponse response = NotificationPageResponse.fromPage(notifications);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user notifications.
     */
    @Operation(
        summary = "Get user notifications",
        description = "Retrieves notifications for a specific user. Users can access their own notifications, admins can access any user's notifications."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User notifications retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<NotificationPageResponse> getUserNotifications(
        @Parameter(description = "User ID", example = "123", required = true)
        @PathVariable Long userId,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Notification> notifications = notificationService.findByUser(userId, pageable);
        NotificationPageResponse response = NotificationPageResponse.fromPage(notifications);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get unread notifications for user.
     */
    @Operation(
        summary = "Get unread notifications",
        description = "Retrieves unread notifications for a specific user. Users can access their own unread notifications, admins can access any user's unread notifications."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<NotificationPageResponse> getUnreadNotifications(
        @Parameter(description = "User ID", example = "123", required = true)
        @PathVariable Long userId,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Notification> notifications = notificationService.findUnreadByUser(userId, pageable);
        NotificationPageResponse response = NotificationPageResponse.fromPage(notifications);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Count unread notifications for user.
     */
    @Operation(
        summary = "Count unread notifications",
        description = "Returns the count of unread notifications for a specific user. Users can access their own count, admins can access any user's count."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
        @Parameter(description = "User ID", example = "123", required = true)
        @PathVariable Long userId) {
        
        long unreadCount = notificationService.countUnreadNotifications(userId);
        UnreadCountResponse response = new UnreadCountResponse(unreadCount);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get notifications by type (admin only).
     */
    @Operation(
        summary = "Get notifications by type",
        description = "Retrieves notifications filtered by type. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "400", description = "Invalid notification type"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationPageResponse> getNotificationsByType(
        @Parameter(description = "Notification type", example = "WELCOME", required = true)
        @PathVariable Notification.NotificationType type,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Notification> notifications = notificationService.findByType(type, pageable);
        NotificationPageResponse response = NotificationPageResponse.fromPage(notifications);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get notifications by status (admin only).
     */
    @Operation(
        summary = "Get notifications by status",
        description = "Retrieves notifications filtered by status. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "400", description = "Invalid notification status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationPageResponse> getNotificationsByStatus(
        @Parameter(description = "Notification status", example = "SENT", required = true)
        @PathVariable Notification.NotificationStatus status,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Notification> notifications = notificationService.findByStatus(status, pageable);
        NotificationPageResponse response = NotificationPageResponse.fromPage(notifications);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get notification by ID.
     */
    @Operation(
        summary = "Get notification by ID",
        description = "Retrieves notification details by ID. Users can access their own notifications, admins can access any notification."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
        @Parameter(description = "Notification ID", example = "789", required = true)
        @PathVariable Long id) {
        
        Notification notification = notificationService.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
            
        NotificationResponse response = NotificationResponse.fromNotification(notification);
        return ResponseEntity.ok(response);
    }

    /**
     * Create and send notification (admin only).
     */
    @Operation(
        summary = "Create notification",
        description = "Creates and sends a new notification to a specific user. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Notification created and sent successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> createNotification(
        @Parameter(description = "Notification creation details", required = true)
        @Valid @RequestBody NotificationCreateRequest request) {
        
        Notification notification = notificationService.createNotification(request.toServiceRequest());
        NotificationResponse response = NotificationResponse.fromNotification(notification);
        
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Send bulk notification (admin only).
     */
    @Operation(
        summary = "Send bulk notification",
        description = "Sends notification to multiple users. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bulk notifications sent successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkNotificationResponse> sendBulkNotification(
        @Parameter(description = "Bulk notification details", required = true)
        @Valid @RequestBody BulkNotificationRequest request) {
        
        List<Notification> notifications = notificationService.sendBulkNotification(
            request.getUserIds(), request.toBulkServiceRequest());
        
        BulkNotificationResponse response = new BulkNotificationResponse(
            notifications.size(), 
            "Bulk notifications sent successfully"
        );
        
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Send notification to role (admin only).
     */
    @Operation(
        summary = "Send notification to role",
        description = "Sends notification to all users with a specific role. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Role notifications sent successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkNotificationResponse> sendNotificationToRole(
        @Parameter(description = "User role", example = "CUSTOMER", required = true)
        @PathVariable User.UserRole role,
        @Parameter(description = "Notification details", required = true)
        @Valid @RequestBody RoleNotificationRequest request) {
        
        List<Notification> notifications = notificationService.sendNotificationToRole(
            role, request.toBulkServiceRequest());
        
        BulkNotificationResponse response = new BulkNotificationResponse(
            notifications.size(), 
            "Role notifications sent successfully"
        );
        
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Mark notification as read.
     */
    @Operation(
        summary = "Mark notification as read",
        description = "Marks a specific notification as read. Users can mark their own notifications, admins can mark any notification."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification marked as read",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
        @Parameter(description = "Notification ID", example = "789", required = true)
        @PathVariable Long id) {
        
        Notification notification = notificationService.markAsRead(id);
        NotificationResponse response = NotificationResponse.fromNotification(notification);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Mark all notifications as read for user.
     */
    @Operation(
        summary = "Mark all notifications as read",
        description = "Marks all notifications as read for a specific user. Users can mark their own notifications, admins can mark any user's notifications."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All notifications marked as read"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<MessageResponse> markAllAsRead(
        @Parameter(description = "User ID", example = "123", required = true)
        @PathVariable Long userId) {
        
        int markedCount = notificationService.markAllAsRead(userId);
        
        MessageResponse response = MessageResponse.builder()
            .message(String.format("Marked %d notifications as read", markedCount))
            .timestamp(LocalDateTime.now())
            .build();
            
        return ResponseEntity.ok(response);
    }

    /**
     * Delete notification.
     */
    @Operation(
        summary = "Delete notification",
        description = "Deletes a notification. Users can delete their own notifications, admins can delete any notification."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
        @Parameter(description = "Notification ID", example = "789", required = true)
        @PathVariable Long id) {
        
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get notification statistics (admin only).
     */
    @Operation(
        summary = "Get notification statistics",
        description = "Retrieves comprehensive notification statistics and analytics. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationService.NotificationStatistics.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationService.NotificationStatistics> getNotificationStatistics() {
        
        NotificationService.NotificationStatistics statistics = notificationService.getNotificationStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Retry failed notifications (admin only).
     */
    @Operation(
        summary = "Retry failed notifications",
        description = "Retries failed notifications within specified hours. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Failed notifications retried successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/retry-failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> retryFailedNotifications(
        @Parameter(description = "Number of hours to look back", example = "24")
        @RequestParam(defaultValue = "24") int hours) {
        
        int retriedCount = notificationService.retryFailedNotifications(hours);
        
        MessageResponse response = MessageResponse.builder()
            .message(String.format("Retried %d failed notifications", retriedCount))
            .timestamp(LocalDateTime.now())
            .build();
            
        return ResponseEntity.ok(response);
    }

    // DTOs for API requests and responses

    /**
     * Notification creation request DTO.
     */
    @Schema(description = "Notification creation request")
    public static class NotificationCreateRequest {
        
        @Schema(description = "User ID", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "User ID is required")
        private Long userId;

        @Schema(description = "Notification type", example = "WELCOME", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Notification type is required")
        private Notification.NotificationType type;

        @Schema(description = "Notification title", example = "Welcome to ShopWave!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Title is required")
        private String title;

        @Schema(description = "Notification message", example = "Thank you for joining us!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Message is required")
        private String message;

        @Schema(description = "Notification priority", example = "HIGH")
        private Notification.NotificationPriority priority;

        @Schema(description = "Delivery channel", example = "EMAIL")
        private Notification.DeliveryChannel channel;

        @Schema(description = "Additional metadata", example = "welcomeBonus:10")
        private String metadata;

        // Constructors
        public NotificationCreateRequest() {}

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Notification.NotificationType getType() { return type; }
        public void setType(Notification.NotificationType type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Notification.NotificationPriority getPriority() { return priority; }
        public void setPriority(Notification.NotificationPriority priority) { this.priority = priority; }

        public Notification.DeliveryChannel getChannel() { return channel; }
        public void setChannel(Notification.DeliveryChannel channel) { this.channel = channel; }

        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }

        public NotificationService.NotificationCreateRequest toServiceRequest() {
            NotificationService.NotificationCreateRequest request = new NotificationService.NotificationCreateRequest();
            request.setUserId(this.userId);
            request.setType(this.type);
            request.setTitle(this.title);
            request.setMessage(this.message);
            request.setPriority(this.priority);
            request.setChannel(this.channel);
            request.setMetadata(this.metadata);
            return request;
        }
    }

    /**
     * Bulk notification request DTO.
     */
    @Schema(description = "Bulk notification request for sending to multiple users")
    public static class BulkNotificationRequest {
        
        @Schema(description = "List of user IDs", example = "[123, 456, 789]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "User IDs are required")
        private List<Long> userIds;

        @Schema(description = "Notification type", example = "PROMOTION", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Notification type is required")
        private Notification.NotificationType type;

        @Schema(description = "Notification title", example = "Special Offer!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Title is required")
        private String title;

        @Schema(description = "Notification message", example = "Get 20% off your next purchase!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Message is required")
        private String message;

        @Schema(description = "Notification priority", example = "HIGH")
        private Notification.NotificationPriority priority;

        @Schema(description = "Delivery channel", example = "EMAIL")
        private Notification.DeliveryChannel channel;

        @Schema(description = "Additional metadata", example = "promoCode:SAVE20")
        private String metadata;

        // Constructors
        public BulkNotificationRequest() {}

        // Getters and setters
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }

        public Notification.NotificationType getType() { return type; }
        public void setType(Notification.NotificationType type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Notification.NotificationPriority getPriority() { return priority; }
        public void setPriority(Notification.NotificationPriority priority) { this.priority = priority; }

        public Notification.DeliveryChannel getChannel() { return channel; }
        public void setChannel(Notification.DeliveryChannel channel) { this.channel = channel; }

        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }

        public NotificationService.BulkNotificationRequest toBulkServiceRequest() {
            NotificationService.BulkNotificationRequest request = new NotificationService.BulkNotificationRequest();
            request.setType(this.type);
            request.setTitle(this.title);
            request.setMessage(this.message);
            request.setPriority(this.priority);
            request.setChannel(this.channel);
            request.setMetadata(this.metadata);
            return request;
        }
    }

    /**
     * Role notification request DTO.
     */
    @Schema(description = "Role notification request for sending to users with specific role")
    public static class RoleNotificationRequest {
        
        @Schema(description = "Notification type", example = "SYSTEM_MAINTENANCE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Notification type is required")
        private Notification.NotificationType type;

        @Schema(description = "Notification title", example = "System Maintenance", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Title is required")
        private String title;

        @Schema(description = "Notification message", example = "System will be down for maintenance", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Message is required")
        private String message;

        @Schema(description = "Notification priority", example = "HIGH")
        private Notification.NotificationPriority priority;

        @Schema(description = "Delivery channel", example = "EMAIL")
        private Notification.DeliveryChannel channel;

        @Schema(description = "Additional metadata", example = "maintenanceWindow:2024-01-15T02:00:00")
        private String metadata;

        // Constructors
        public RoleNotificationRequest() {}

        // Getters and setters
        public Notification.NotificationType getType() { return type; }
        public void setType(Notification.NotificationType type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Notification.NotificationPriority getPriority() { return priority; }
        public void setPriority(Notification.NotificationPriority priority) { this.priority = priority; }

        public Notification.DeliveryChannel getChannel() { return channel; }
        public void setChannel(Notification.DeliveryChannel channel) { this.channel = channel; }

        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }

        public NotificationService.BulkNotificationRequest toBulkServiceRequest() {
            NotificationService.BulkNotificationRequest request = new NotificationService.BulkNotificationRequest();
            request.setType(this.type);
            request.setTitle(this.title);
            request.setMessage(this.message);
            request.setPriority(this.priority);
            request.setChannel(this.channel);
            request.setMetadata(this.metadata);
            return request;
        }
    }

    /**
     * Notification response DTO.
     */
    @Schema(description = "Notification information response")
    public static class NotificationResponse {
        
        @Schema(description = "Notification unique identifier", example = "789")
        private Long id;

        @Schema(description = "Notification type", example = "WELCOME")
        private String type;

        @Schema(description = "Notification title", example = "Welcome to ShopWave!")
        private String title;

        @Schema(description = "Notification message", example = "Thank you for joining us!")
        private String message;

        @Schema(description = "Delivery channel", example = "EMAIL")
        private String deliveryChannel;

        @Schema(description = "Notification priority", example = "HIGH")
        private String priority;

        @Schema(description = "Notification status", example = "SENT")
        private String status;

        @Schema(description = "User ID", example = "123")
        private Long userId;

        @Schema(description = "User email", example = "user@example.com")
        private String userEmail;

        @Schema(description = "Read status", example = "false")
        private Boolean read;

        @Schema(description = "Additional metadata", example = "welcomeBonus:10")
        private String metadata;

        @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "Sent timestamp", example = "2024-01-15T10:30:05")
        private LocalDateTime sentAt;

        @Schema(description = "Read timestamp", example = "2024-01-15T11:00:00")
        private LocalDateTime readAt;

        public static NotificationResponse fromNotification(Notification notification) {
            NotificationResponse response = new NotificationResponse();
            response.id = notification.getId();
            response.type = notification.getType().name();
            response.title = notification.getTitle();
            response.message = notification.getMessage();
            response.deliveryChannel = notification.getDeliveryChannel().name();
            response.priority = notification.getPriority().name();
            response.status = notification.getStatus().name();
            response.userId = notification.getUser().getId();
            response.userEmail = notification.getUser().getEmail();
            response.read = notification.isRead();
            response.metadata = notification.getMetadata();
            response.createdAt = notification.getCreatedAt();
            response.sentAt = notification.getSentAt();
            response.readAt = notification.getReadAt();
            return response;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getDeliveryChannel() { return deliveryChannel; }
        public void setDeliveryChannel(String deliveryChannel) { this.deliveryChannel = deliveryChannel; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public Boolean getRead() { return read; }
        public void setRead(Boolean read) { this.read = read; }

        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getSentAt() { return sentAt; }
        public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

        public LocalDateTime getReadAt() { return readAt; }
        public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    }

    /**
     * Paginated notification response DTO.
     */
    @Schema(description = "Paginated notification response")
    public static class NotificationPageResponse {
        
        @Schema(description = "List of notifications")
        private List<NotificationResponse> notifications;

        @Schema(description = "Current page number", example = "0")
        private int page;

        @Schema(description = "Page size", example = "20")
        private int size;

        @Schema(description = "Total number of elements", example = "200")
        private long totalElements;

        @Schema(description = "Total number of pages", example = "10")
        private int totalPages;

        @Schema(description = "Is first page", example = "true")
        private boolean first;

        @Schema(description = "Is last page", example = "false")
        private boolean last;

        public static NotificationPageResponse fromPage(Page<Notification> page) {
            NotificationPageResponse response = new NotificationPageResponse();
            response.notifications = page.getContent().stream()
                .map(NotificationResponse::fromNotification)
                .toList();
            response.page = page.getNumber();
            response.size = page.getSize();
            response.totalElements = page.getTotalElements();
            response.totalPages = page.getTotalPages();
            response.first = page.isFirst();
            response.last = page.isLast();
            return response;
        }

        // Getters and setters
        public List<NotificationResponse> getNotifications() { return notifications; }
        public void setNotifications(List<NotificationResponse> notifications) { this.notifications = notifications; }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public boolean isFirst() { return first; }
        public void setFirst(boolean first) { this.first = first; }

        public boolean isLast() { return last; }
        public void setLast(boolean last) { this.last = last; }
    }

    /**
     * Unread count response DTO.
     */
    @Schema(description = "Unread notification count response")
    public static class UnreadCountResponse {
        
        @Schema(description = "Number of unread notifications", example = "5")
        private long unreadCount;

        public UnreadCountResponse(long unreadCount) {
            this.unreadCount = unreadCount;
        }

        // Getters and setters
        public long getUnreadCount() { return unreadCount; }
        public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
    }

    /**
     * Bulk notification response DTO.
     */
    @Schema(description = "Bulk notification operation response")
    public static class BulkNotificationResponse {
        
        @Schema(description = "Number of notifications sent", example = "25")
        private int notificationsSent;

        @Schema(description = "Operation message", example = "Bulk notifications sent successfully")
        private String message;

        public BulkNotificationResponse(int notificationsSent, String message) {
            this.notificationsSent = notificationsSent;
            this.message = message;
        }

        // Getters and setters
        public int getNotificationsSent() { return notificationsSent; }
        public void setNotificationsSent(int notificationsSent) { this.notificationsSent = notificationsSent; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * Message response DTO.
     */
    @Schema(description = "Standard message response")
    public static class MessageResponse {
        
        @Schema(description = "Response message", example = "Operation completed successfully")
        private String message;

        @Schema(description = "Response timestamp", example = "2024-01-15T10:30:00")
        private LocalDateTime timestamp;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String message;
            private LocalDateTime timestamp;

            public Builder message(String message) { this.message = message; return this; }
            public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

            public MessageResponse build() {
                MessageResponse response = new MessageResponse();
                response.message = this.message;
                response.timestamp = this.timestamp;
                return response;
            }
        }

        // Getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}