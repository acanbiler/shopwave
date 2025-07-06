package com.acanbiler.shopwave.controller;

import com.acanbiler.shopwave.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Authentication controller handling user registration, login, and token management.
 * 
 * This controller provides endpoints for user authentication, OAuth2 integration,
 * and JWT token management with comprehensive validation and error handling.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and authorization operations")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user account.
     */
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account with email and password. Returns JWT token for immediate authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed or email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Parameter(description = "User registration details", required = true)
        @Valid @RequestBody RegisterRequest request) {
        
        AuthService.AuthResponse serviceResponse = authService.register(request.toServiceRequest());
        
        AuthResponse response = AuthResponse.builder()
            .token(serviceResponse.getAccessToken())
            .refreshToken(serviceResponse.getRefreshToken())
            .expiresAt(LocalDateTime.now().plusSeconds(serviceResponse.getExpiresIn()))
            .user(UserResponse.fromUserInfo(serviceResponse.getUser()))
            .build();
            
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticate user with email and password.
     */
    @Operation(
        summary = "User login",
        description = "Authenticates user with email and password. Returns JWT token and user information."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Parameter(description = "User login credentials", required = true)
        @Valid @RequestBody LoginRequest request) {
        
        AuthService.AuthResponse serviceResponse = authService.authenticate(request.getEmail(), request.getPassword());
        
        AuthResponse response = AuthResponse.builder()
            .token(serviceResponse.getAccessToken())
            .refreshToken(serviceResponse.getRefreshToken())
            .expiresAt(LocalDateTime.now().plusSeconds(serviceResponse.getExpiresIn()))
            .user(UserResponse.fromUserInfo(serviceResponse.getUser()))
            .build();
            
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh JWT token using refresh token.
     */
    @Operation(
        summary = "Refresh JWT token",
        description = "Generates new JWT token using valid refresh token. Extends user session without re-authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
        @Parameter(description = "Refresh token request", required = true)
        @Valid @RequestBody RefreshTokenRequest request) {
        
        AuthService.AuthResponse serviceResponse = authService.refreshToken(request.getRefreshToken());
        
        TokenResponse response = TokenResponse.builder()
            .token(serviceResponse.getAccessToken())
            .expiresAt(LocalDateTime.now().plusSeconds(serviceResponse.getExpiresIn()))
            .build();
            
        return ResponseEntity.ok(response);
    }

    /**
     * Validate token (utility endpoint).
     */
    @Operation(
        summary = "Validate token",
        description = "Validates JWT token and returns user information."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/validate")
    public ResponseEntity<UserResponse> validateToken(
        @Parameter(description = "Authorization header with Bearer token", required = true)
        @RequestHeader("Authorization") String authorization) {
        
        String token = authorization.replace("Bearer ", "");
        AuthService.UserInfo userInfo = authService.validateToken(token);
        UserResponse response = UserResponse.fromUserInfo(userInfo);
        
        return ResponseEntity.ok(response);
    }


    // DTOs for API requests and responses

    /**
     * User registration request DTO.
     */
    @Schema(description = "User registration request containing all required user information")
    public static class RegisterRequest {
        
        @Schema(description = "User email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        private String email;

        @Schema(description = "User password", example = "SecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        private String password;

        @Schema(description = "User first name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
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

        // Constructors
        public RegisterRequest() {}

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

        public AuthService.UserRegistrationRequest toServiceRequest() {
            AuthService.UserRegistrationRequest request = new AuthService.UserRegistrationRequest();
            request.setEmail(this.email);
            request.setPassword(this.password);
            request.setFirstName(this.firstName);
            request.setLastName(this.lastName);
            request.setPhoneNumber(this.phoneNumber);
            return request;
        }
    }

    /**
     * User login request DTO.
     */
    @Schema(description = "User login request with email and password")
    public static class LoginRequest {
        
        @Schema(description = "User email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @Schema(description = "User password", example = "SecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        private String password;

        // Constructors
        public LoginRequest() {}

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * Refresh token request DTO.
     */
    @Schema(description = "Refresh token request")
    public static class RefreshTokenRequest {
        
        @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;

        // Constructors
        public RefreshTokenRequest() {}

        // Getters and setters
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }


    /**
     * Authentication response DTO.
     */
    @Schema(description = "Authentication response with JWT token and user information")
    public static class AuthResponse {
        
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String token;

        @Schema(description = "Refresh token for token renewal", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String refreshToken;

        @Schema(description = "Token expiration time", example = "2024-12-31T23:59:59")
        private LocalDateTime expiresAt;

        @Schema(description = "Authenticated user information")
        private UserResponse user;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String token;
            private String refreshToken;
            private LocalDateTime expiresAt;
            private UserResponse user;

            public Builder token(String token) { this.token = token; return this; }
            public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
            public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
            public Builder user(UserResponse user) { this.user = user; return this; }

            public AuthResponse build() {
                AuthResponse response = new AuthResponse();
                response.token = this.token;
                response.refreshToken = this.refreshToken;
                response.expiresAt = this.expiresAt;
                response.user = this.user;
                return response;
            }
        }

        // Getters and setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

        public UserResponse getUser() { return user; }
        public void setUser(UserResponse user) { this.user = user; }
    }

    /**
     * Token response DTO.
     */
    @Schema(description = "Token response for refresh operations")
    public static class TokenResponse {
        
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String token;

        @Schema(description = "Token expiration time", example = "2024-12-31T23:59:59")
        private LocalDateTime expiresAt;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String token;
            private LocalDateTime expiresAt;

            public Builder token(String token) { this.token = token; return this; }
            public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }

            public TokenResponse build() {
                TokenResponse response = new TokenResponse();
                response.token = this.token;
                response.expiresAt = this.expiresAt;
                return response;
            }
        }

        // Getters and setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
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

    /**
     * User response DTO (simplified user information).
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

        @Schema(description = "User role", example = "CUSTOMER")
        private String role;

        @Schema(description = "Email verification status", example = "true")
        private Boolean emailVerified;

        public static UserResponse fromUser(com.acanbiler.shopwave.entity.User user) {
            UserResponse response = new UserResponse();
            response.id = user.getId();
            response.email = user.getEmail();
            response.firstName = user.getFirstName();
            response.lastName = user.getLastName();
            response.role = user.getRole().name();
            response.emailVerified = user.getEmailVerified();
            return response;
        }

        public static UserResponse fromUserInfo(AuthService.UserInfo userInfo) {
            UserResponse response = new UserResponse();
            response.id = userInfo.getId();
            response.email = userInfo.getEmail();
            response.firstName = userInfo.getFirstName();
            response.lastName = userInfo.getLastName();
            response.role = userInfo.getRole();
            response.emailVerified = userInfo.isEmailVerified();
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

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public Boolean getEmailVerified() { return emailVerified; }
        public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    }
}