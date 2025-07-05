package com.acanbiler.shopwave.controller;

import com.acanbiler.shopwave.entity.User;
import com.acanbiler.shopwave.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
 * User management controller for admin and user profile operations.
 * 
 * This controller provides endpoints for user CRUD operations, profile management,
 * and administrative functions with proper authorization controls.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User administration and profile management operations")
@SecurityRequirement(name = "Bearer Authentication")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users (admin only).
     */
    @Operation(
        summary = "Get all users",
        description = "Retrieves paginated list of all users. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPageResponse> getAllUsers(
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<User> users = userService.findAll(pageable);
        UserPageResponse response = UserPageResponse.fromPage(users);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Search users by keyword (admin only).
     */
    @Operation(
        summary = "Search users",
        description = "Searches users by email, first name, or last name. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPageResponse> searchUsers(
        @Parameter(description = "Search keyword", example = "john", required = true)
        @RequestParam String keyword,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<User> users = userService.searchUsers(keyword, pageable);
        UserPageResponse response = UserPageResponse.fromPage(users);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get users by role (admin only).
     */
    @Operation(
        summary = "Get users by role",
        description = "Retrieves users filtered by role (ADMIN or CUSTOMER). Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "400", description = "Invalid role specified"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPageResponse> getUsersByRole(
        @Parameter(description = "User role", example = "CUSTOMER", required = true)
        @PathVariable User.UserRole role,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<User> users = userService.findByRole(role, pageable);
        UserPageResponse response = UserPageResponse.fromPage(users);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID.
     */
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves user details by ID. Users can access their own profile, admins can access any user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
        @Parameter(description = "User ID", example = "123", required = true)
        @PathVariable Long id) {
        
        User user = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        UserResponse response = UserResponse.fromUser(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Create new user (admin only).
     */
    @Operation(
        summary = "Create new user",
        description = "Creates a new user account. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed or email already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
        @Parameter(description = "User creation details", required = true)
        @Valid @RequestBody UserCreateRequest request) {
        
        User user = userService.createUser(request.toServiceRequest());
        UserResponse response = UserResponse.fromUser(user);
        
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Update user information.
     */
    @Operation(
        summary = "Update user",
        description = "Updates user information. Users can update their own profile, admins can update any user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
        @Parameter(description = "User ID", example = "123", required = true)
        @PathVariable Long id,
        @Parameter(description = "User update details", required = true)
        @Valid @RequestBody UserUpdateRequest request) {
        
        User user = userService.updateUser(id, request.toServiceRequest());
        UserResponse response = UserResponse.fromUser(user);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete user (admin only).
     */
    @Operation(
        summary = "Delete user",
        description = "Permanently deletes a user account and all associated data. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
        @Parameter(description = "User ID", example = "123", required = true)
        @PathVariable Long id) {
        
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Enable or disable user account (admin only).
     */
    @Operation(
        summary = "Enable/disable user",
        description = "Enables or disables a user account. Disabled users cannot log in. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> setUserEnabled(
        @Parameter(description = "User ID", example = "123", required = true)
        @PathVariable Long id,
        @Parameter(description = "Enable status", example = "true", required = true)
        @RequestParam boolean enabled) {
        
        userService.setUserEnabled(id, enabled);
        
        MessageResponse response = MessageResponse.builder()
            .message("User " + (enabled ? "enabled" : "disabled") + " successfully")
            .timestamp(LocalDateTime.now())
            .build();
            
        return ResponseEntity.ok(response);
    }

    /**
     * Get user statistics (admin only).
     */
    @Operation(
        summary = "Get user statistics",
        description = "Retrieves comprehensive user statistics and analytics. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserService.UserStatistics.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserService.UserStatistics> getUserStatistics() {
        
        UserService.UserStatistics statistics = userService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get inactive users (admin only).
     */
    @Operation(
        summary = "Get inactive users",
        description = "Retrieves list of users who haven't logged in for specified number of days. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inactive users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getInactiveUsers(
        @Parameter(description = "Number of days of inactivity", example = "30")
        @RequestParam(defaultValue = "30") int days) {
        
        List<User> users = userService.getInactiveUsers(days);
        List<UserResponse> response = users.stream()
            .map(UserResponse::fromUser)
            .toList();
            
        return ResponseEntity.ok(response);
    }

    /**
     * Get users who never logged in (admin only).
     */
    @Operation(
        summary = "Get users who never logged in",
        description = "Retrieves list of users who have never logged in to the system. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/never-logged-in")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsersWhoNeverLoggedIn() {
        
        List<User> users = userService.getUsersWhoNeverLoggedIn();
        List<UserResponse> response = users.stream()
            .map(UserResponse::fromUser)
            .toList();
            
        return ResponseEntity.ok(response);
    }

    // DTOs for API requests and responses

    /**
     * User creation request DTO.
     */
    @Schema(description = "User creation request containing all required user information")
    public static class UserCreateRequest {
        
        @Schema(description = "User email address", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        private String email;

        @Schema(description = "User password", example = "SecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        private String password;

        @Schema(description = "User first name", example = "Jane", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        private String firstName;

        @Schema(description = "User last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        private String lastName;

        @Schema(description = "User phone number", example = "+1234567890")
        @Size(max = 15, message = "Phone number must not exceed 15 characters")
        private String phoneNumber;

        @Schema(description = "User role", example = "CUSTOMER")
        private User.UserRole role;

        @Schema(description = "Account enabled status", example = "true")
        private Boolean enabled;

        @Schema(description = "Email verification status", example = "false")
        private Boolean emailVerified;

        // Constructors
        public UserCreateRequest() {}

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public User.UserRole getRole() { return role; }
        public void setRole(User.UserRole role) { this.role = role; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public Boolean getEmailVerified() { return emailVerified; }
        public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

        public UserService.UserCreateRequest toServiceRequest() {
            UserService.UserCreateRequest request = new UserService.UserCreateRequest();
            request.setEmail(this.email);
            request.setPassword(this.password);
            request.setFirstName(this.firstName);
            request.setLastName(this.lastName);
            request.setPhoneNumber(this.phoneNumber);
            request.setRole(this.role);
            request.setEnabled(this.enabled);
            request.setEmailVerified(this.emailVerified);
            return request;
        }
    }

    /**
     * User update request DTO.
     */
    @Schema(description = "User update request for modifying user information")
    public static class UserUpdateRequest {
        
        @Schema(description = "User first name", example = "Jane")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        private String firstName;

        @Schema(description = "User last name", example = "Smith")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        private String lastName;

        @Schema(description = "User phone number", example = "+1987654321")
        @Size(max = 15, message = "Phone number must not exceed 15 characters")
        private String phoneNumber;

        @Schema(description = "Profile picture URL", example = "https://example.com/profile.jpg")
        private String profilePictureUrl;

        @Schema(description = "User role (admin only)", example = "CUSTOMER")
        private User.UserRole role;

        @Schema(description = "Account enabled status (admin only)", example = "true")
        private Boolean enabled;

        @Schema(description = "Email verification status (admin only)", example = "true")
        private Boolean emailVerified;

        // Constructors
        public UserUpdateRequest() {}

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

        public User.UserRole getRole() { return role; }
        public void setRole(User.UserRole role) { this.role = role; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public Boolean getEmailVerified() { return emailVerified; }
        public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

        public UserService.UserUpdateRequest toServiceRequest() {
            UserService.UserUpdateRequest request = new UserService.UserUpdateRequest();
            request.setFirstName(this.firstName);
            request.setLastName(this.lastName);
            request.setPhoneNumber(this.phoneNumber);
            request.setProfilePictureUrl(this.profilePictureUrl);
            request.setRole(this.role);
            request.setEnabled(this.enabled);
            request.setEmailVerified(this.emailVerified);
            return request;
        }
    }

    /**
     * User response DTO.
     */
    @Schema(description = "User information response")
    public static class UserResponse {
        
        @Schema(description = "User unique identifier", example = "123")
        private Long id;

        @Schema(description = "User email address", example = "john.doe@example.com")
        private String email;

        @Schema(description = "User first name", example = "John")
        private String firstName;

        @Schema(description = "User last name", example = "Doe")
        private String lastName;

        @Schema(description = "User full name", example = "John Doe")
        private String fullName;

        @Schema(description = "User phone number", example = "+1234567890")
        private String phoneNumber;

        @Schema(description = "User role", example = "CUSTOMER")
        private String role;

        @Schema(description = "Account enabled status", example = "true")
        private Boolean enabled;

        @Schema(description = "Email verification status", example = "true")
        private Boolean emailVerified;

        @Schema(description = "Profile picture URL", example = "https://example.com/profile.jpg")
        private String profilePictureUrl;

        @Schema(description = "Last login timestamp", example = "2024-01-15T10:30:00")
        private LocalDateTime lastLogin;

        @Schema(description = "Account creation timestamp", example = "2024-01-01T00:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00")
        private LocalDateTime updatedAt;

        public static UserResponse fromUser(User user) {
            UserResponse response = new UserResponse();
            response.id = user.getId();
            response.email = user.getEmail();
            response.firstName = user.getFirstName();
            response.lastName = user.getLastName();
            response.fullName = user.getFullName();
            response.phoneNumber = user.getPhoneNumber();
            response.role = user.getRole().name();
            response.enabled = user.getEnabled();
            response.emailVerified = user.getEmailVerified();
            response.profilePictureUrl = user.getProfilePictureUrl();
            response.lastLogin = user.getLastLogin();
            response.createdAt = user.getCreatedAt();
            response.updatedAt = user.getUpdatedAt();
            return response;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public Boolean getEmailVerified() { return emailVerified; }
        public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

        public LocalDateTime getLastLogin() { return lastLogin; }
        public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * Paginated user response DTO.
     */
    @Schema(description = "Paginated user response")
    public static class UserPageResponse {
        
        @Schema(description = "List of users")
        private List<UserResponse> users;

        @Schema(description = "Current page number", example = "0")
        private int page;

        @Schema(description = "Page size", example = "20")
        private int size;

        @Schema(description = "Total number of elements", example = "100")
        private long totalElements;

        @Schema(description = "Total number of pages", example = "5")
        private int totalPages;

        @Schema(description = "Is first page", example = "true")
        private boolean first;

        @Schema(description = "Is last page", example = "false")
        private boolean last;

        public static UserPageResponse fromPage(Page<User> page) {
            UserPageResponse response = new UserPageResponse();
            response.users = page.getContent().stream()
                .map(UserResponse::fromUser)
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
        public List<UserResponse> getUsers() { return users; }
        public void setUsers(List<UserResponse> users) { this.users = users; }

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