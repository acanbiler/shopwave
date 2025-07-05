package com.acanbiler.shopwave.repository;

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
 * Repository interface for User entity operations.
 * 
 * This repository provides basic CRUD operations and custom query methods
 * for user management and authentication.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic finder methods
    Optional<User> findByEmail(String email);
    
    Optional<User> findByGoogleId(String googleId);
    
    boolean existsByEmail(String email);
    
    boolean existsByGoogleId(String googleId);

    // Role-based queries
    List<User> findByRole(User.UserRole role);
    
    Page<User> findByRole(User.UserRole role, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.UserRole role);

    // Status-based queries
    List<User> findByEnabled(boolean enabled);
    
    Page<User> findByEnabled(boolean enabled, Pageable pageable);
    
    List<User> findByEmailVerified(boolean emailVerified);
    
    Page<User> findByEmailVerified(boolean emailVerified, Pageable pageable);

    // Search queries
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
           "u.role = :role AND (" +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchUsersByRole(@Param("keyword") String keyword, 
                                @Param("role") User.UserRole role, 
                                Pageable pageable);

    // Time-based queries
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<User> findByLastLoginBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL")
    List<User> findUsersWhoNeverLoggedIn();
    
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date")
    List<User> findInactiveUsersSince(@Param("date") LocalDateTime date);

    // Statistics queries
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserCountByRole();
    
    @Query("SELECT DATE(u.createdAt), COUNT(u) FROM User u " +
           "WHERE u.createdAt >= :startDate " +
           "GROUP BY DATE(u.createdAt) " +
           "ORDER BY DATE(u.createdAt)")
    List<Object[]> getUserRegistrationStats(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT " +
           "COUNT(u) as totalUsers, " +
           "COUNT(CASE WHEN u.enabled = true THEN 1 END) as activeUsers, " +
           "COUNT(CASE WHEN u.emailVerified = true THEN 1 END) as verifiedUsers, " +
           "COUNT(CASE WHEN u.lastLogin IS NOT NULL THEN 1 END) as usersWhoLoggedIn " +
           "FROM User u")
    Object getUserStatistics();

    // Update operations
    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :id")
    int updateUserEnabledStatus(@Param("id") Long id, @Param("enabled") boolean enabled);
    
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.email = :email")
    int markEmailAsVerified(@Param("email") String email);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :id")
    int updateLastLogin(@Param("id") Long id, @Param("loginTime") LocalDateTime loginTime);
    
    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :id")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    // OAuth2 related queries
    @Query("SELECT u FROM User u WHERE u.googleId IS NOT NULL")
    List<User> findOAuth2Users();
    
    @Query("SELECT u FROM User u WHERE u.googleId IS NULL")
    List<User> findNonOAuth2Users();

    // Admin queries
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.enabled = true")
    List<User> findActiveAdmins();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN'")
    long countAdmins();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER'")
    long countCustomers();

    // Verification queries
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :date")
    List<User> findUnverifiedUsersOlderThan(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = false")
    long countUnverifiedUsers();

    // Complex queries for analytics
    @Query("SELECT " +
           "EXTRACT(YEAR FROM u.createdAt) as year, " +
           "EXTRACT(MONTH FROM u.createdAt) as month, " +
           "COUNT(u) as count " +
           "FROM User u " +
           "WHERE u.createdAt >= :startDate " +
           "GROUP BY EXTRACT(YEAR FROM u.createdAt), EXTRACT(MONTH FROM u.createdAt) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyRegistrationStats(@Param("startDate") LocalDateTime startDate);
}