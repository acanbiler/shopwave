package com.acanbiler.shopwave.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acanbiler.shopwave.entity.User;
import com.acanbiler.shopwave.repository.UserRepository;
import com.acanbiler.shopwave.util.JwtUtil;

/**
 * Authentication service for ShopWave application.
 * 
 * This service handles user authentication, registration, and JWT token management
 * with support for both email/password and OAuth2 authentication.
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Authenticate user with email and password.
     * 
     * @param email user email
     * @param password user password
     * @return authentication response with tokens
     * @throws AuthenticationException if authentication fails
     */
    public AuthResponse authenticate(String email, String password) {
        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );

            User user = (User) authentication.getPrincipal();
            
            // Update last login time
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            List<String> roles = List.of(user.getRole().name());
            String accessToken = jwtUtil.generateAccessToken(user, user.getId(), roles);
            String refreshToken = jwtUtil.generateRefreshToken(user, user.getId());

            return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(UserInfo.fromUser(user))
                .build();

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password", e);
        }
    }

    /**
     * Register a new user.
     * 
     * @param registrationRequest user registration data
     * @return authentication response with tokens
     * @throws RuntimeException if email already exists
     */
    public AuthResponse register(UserRegistrationRequest registrationRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = User.builder()
            .email(registrationRequest.getEmail())
            .password(passwordEncoder.encode(registrationRequest.getPassword()))
            .firstName(registrationRequest.getFirstName())
            .lastName(registrationRequest.getLastName())
            .phoneNumber(registrationRequest.getPhoneNumber())
            .role(User.UserRole.CUSTOMER)
            .enabled(true)
            .emailVerified(false)
            .build();

        user = userRepository.save(user);

        // Generate tokens
        List<String> roles = List.of(user.getRole().name());
        String accessToken = jwtUtil.generateAccessToken(user, user.getId(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user, user.getId());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getExpirationTime())
            .user(UserInfo.fromUser(user))
            .build();
    }

    /**
     * Refresh access token using refresh token.
     * 
     * @param refreshToken refresh token
     * @return new authentication response with tokens
     * @throws RuntimeException if refresh token is invalid
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            String email = jwtUtil.extractUsername(refreshToken);
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateRefreshToken(refreshToken, user)) {
                throw new RuntimeException("Invalid refresh token");
            }

            // Generate new tokens
            List<String> roles = List.of(user.getRole().name());
            String newAccessToken = jwtUtil.generateAccessToken(user, user.getId(), roles);
            String newRefreshToken = jwtUtil.generateRefreshToken(user, user.getId());

            return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(UserInfo.fromUser(user))
                .build();

        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token", e);
        }
    }

    /**
     * Handle OAuth2 authentication success.
     * 
     * @param email OAuth2 user email
     * @param firstName OAuth2 user first name
     * @param lastName OAuth2 user last name
     * @param googleId OAuth2 provider ID
     * @param profilePictureUrl OAuth2 profile picture URL
     * @return authentication response with tokens
     */
    public AuthResponse handleOAuth2Success(String email, String firstName, String lastName, 
                                          String googleId, String profilePictureUrl) {
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                // Create new user for OAuth2
                User newUser = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("OAUTH2_USER")) // Placeholder password
                    .firstName(firstName)
                    .lastName(lastName)
                    .googleId(googleId)
                    .profilePictureUrl(profilePictureUrl)
                    .role(User.UserRole.CUSTOMER)
                    .enabled(true)
                    .emailVerified(true) // OAuth2 emails are pre-verified
                    .build();
                return userRepository.save(newUser);
            });

        // Update OAuth2 info if user exists
        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
            user.setProfilePictureUrl(profilePictureUrl);
            user.setEmailVerified(true);
            user = userRepository.save(user);
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        List<String> roles = List.of(user.getRole().name());
        String accessToken = jwtUtil.generateAccessToken(user, user.getId(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user, user.getId());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getExpirationTime())
            .user(UserInfo.fromUser(user))
            .build();
    }

    /**
     * Validate access token.
     * 
     * @param token access token
     * @return user info if token is valid
     * @throws RuntimeException if token is invalid
     */
    public UserInfo validateToken(String token) {
        try {
            String email = jwtUtil.extractUsername(token);
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(token, user)) {
                throw new RuntimeException("Invalid token");
            }

            return UserInfo.fromUser(user);

        } catch (Exception e) {
            throw new RuntimeException("Invalid token", e);
        }
    }

    /**
     * Change user password.
     * 
     * @param userId user ID
     * @param currentPassword current password
     * @param newPassword new password
     * @throws RuntimeException if current password is incorrect
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Reset password (for admin use).
     * 
     * @param userId user ID
     * @param newPassword new password
     */
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * User registration request DTO.
     */
    public static class UserRegistrationRequest {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String phoneNumber;

        // Constructors
        public UserRegistrationRequest() {}

        public UserRegistrationRequest(String email, String password, String firstName, 
                                     String lastName, String phoneNumber) {
            this.email = email;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phoneNumber = phoneNumber;
        }

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
    }

    /**
     * Authentication response DTO.
     */
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserInfo user;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String accessToken;
            private String refreshToken;
            private String tokenType;
            private Long expiresIn;
            private UserInfo user;

            public Builder accessToken(String accessToken) {
                this.accessToken = accessToken;
                return this;
            }

            public Builder refreshToken(String refreshToken) {
                this.refreshToken = refreshToken;
                return this;
            }

            public Builder tokenType(String tokenType) {
                this.tokenType = tokenType;
                return this;
            }

            public Builder expiresIn(Long expiresIn) {
                this.expiresIn = expiresIn;
                return this;
            }

            public Builder user(UserInfo user) {
                this.user = user;
                return this;
            }

            public AuthResponse build() {
                AuthResponse response = new AuthResponse();
                response.accessToken = this.accessToken;
                response.refreshToken = this.refreshToken;
                response.tokenType = this.tokenType;
                response.expiresIn = this.expiresIn;
                response.user = this.user;
                return response;
            }
        }

        // Getters and setters
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        
        public Long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
        
        public UserInfo getUser() { return user; }
        public void setUser(UserInfo user) { this.user = user; }
    }

    /**
     * User info DTO.
     */
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String role;
        private boolean enabled;
        private boolean emailVerified;
        private LocalDateTime lastLogin;

        public static UserInfo fromUser(User user) {
            UserInfo userInfo = new UserInfo();
            userInfo.id = user.getId();
            userInfo.email = user.getEmail();
            userInfo.firstName = user.getFirstName();
            userInfo.lastName = user.getLastName();
            userInfo.phoneNumber = user.getPhoneNumber();
            userInfo.role = user.getRole().name();
            userInfo.enabled = user.getEnabled();
            userInfo.emailVerified = user.getEmailVerified();
            userInfo.lastLogin = user.getLastLogin();
            return userInfo;
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
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public boolean isEmailVerified() { return emailVerified; }
        public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
        
        public LocalDateTime getLastLogin() { return lastLogin; }
        public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    }
}