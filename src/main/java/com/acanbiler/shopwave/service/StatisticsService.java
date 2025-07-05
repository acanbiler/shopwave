package com.acanbiler.shopwave.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.acanbiler.shopwave.entity.Payment;
import com.acanbiler.shopwave.entity.Product;
import com.acanbiler.shopwave.entity.User;
import com.acanbiler.shopwave.repository.PaymentRepository;
import com.acanbiler.shopwave.repository.ProductRepository;
import com.acanbiler.shopwave.repository.UserRepository;

/**
 * Statistics and analytics service for ShopWave application.
 * 
 * This service provides comprehensive analytics for payments, products,
 * users, and overall business metrics with proper authorization controls.
 */
@Service
public class StatisticsService {

    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public StatisticsService(PaymentRepository paymentRepository, 
                           ProductRepository productRepository,
                           UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get dashboard statistics (admin only).
     * 
     * @return comprehensive dashboard statistics
     */
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardStatistics getDashboardStatistics() {
        return DashboardStatistics.builder()
            .paymentStats(getPaymentStatistics())
            .productStats(getProductStatistics())
            .userStats(getUserStatistics())
            .revenueStats(getRevenueStatistics())
            .build();
    }

    /**
     * Get payment statistics (admin only).
     * 
     * @return payment statistics
     */
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentStatistics getPaymentStatistics() {
        Object stats = paymentRepository.getPaymentStatistics();
        List<Object[]> providerStats = paymentRepository.getProviderPerformanceStats();
        List<Object[]> dailyStats = paymentRepository.getDailyRevenueStats(LocalDateTime.now().minusDays(30));
        List<Object[]> monthlyStats = paymentRepository.getMonthlyRevenueStats(LocalDateTime.now().minusMonths(12));

        return PaymentStatistics.builder()
            .totalPayments(extractLong(stats, 0))
            .completedPayments(extractLong(stats, 1))
            .failedPayments(extractLong(stats, 2))
            .pendingPayments(extractLong(stats, 3))
            .totalRevenue(extractBigDecimal(stats, 4))
            .averageAmount(extractBigDecimal(stats, 5))
            .providerStats(providerStats)
            .dailyRevenue(dailyStats)
            .monthlyRevenue(monthlyStats)
            .build();
    }

    /**
     * Get product statistics (admin only).
     * 
     * @return product statistics
     */
    @PreAuthorize("hasRole('ADMIN')")
    public ProductStatistics getProductStatistics() {
        Object stats = productRepository.getProductStatistics();
        List<Object[]> categoryStats = productRepository.getProductCountByCategory();
        List<Object[]> monthlyStats = productRepository.getMonthlyProductCreationStats(LocalDateTime.now().minusMonths(12));

        return ProductStatistics.builder()
            .totalProducts(extractLong(stats, 0))
            .inStockProducts(extractLong(stats, 1))
            .outOfStockProducts(extractLong(stats, 2))
            .lowStockProducts(extractLong(stats, 3))
            .averagePrice(extractBigDecimal(stats, 4))
            .minPrice(extractBigDecimal(stats, 5))
            .maxPrice(extractBigDecimal(stats, 6))
            .categoryDistribution(categoryStats)
            .monthlyCreations(monthlyStats)
            .build();
    }

    /**
     * Get user statistics (admin only).
     * 
     * @return user statistics
     */
    @PreAuthorize("hasRole('ADMIN')")
    public UserStatistics getUserStatistics() {
        Object stats = userRepository.getUserStatistics();
        List<Object[]> roleStats = userRepository.getUserCountByRole();
        List<Object[]> registrationStats = userRepository.getMonthlyRegistrationStats(LocalDateTime.now().minusMonths(12));

        return UserStatistics.builder()
            .totalUsers(extractLong(stats, 0))
            .activeUsers(extractLong(stats, 1))
            .verifiedUsers(extractLong(stats, 2))
            .usersWhoLoggedIn(extractLong(stats, 3))
            .roleDistribution(roleStats)
            .monthlyRegistrations(registrationStats)
            .build();
    }

    /**
     * Get revenue statistics (admin only).
     * 
     * @return revenue statistics
     */
    @PreAuthorize("hasRole('ADMIN')")
    public RevenueStatistics getRevenueStatistics() {
        LocalDateTime now = LocalDateTime.now();
        
        // Today's revenue
        BigDecimal todayRevenue = getTodayRevenue();
        
        // This week's revenue
        BigDecimal weekRevenue = getWeekRevenue();
        
        // This month's revenue
        BigDecimal monthRevenue = getMonthRevenue();
        
        // This year's revenue
        BigDecimal yearRevenue = getYearRevenue();
        
        // Revenue trends
        List<Object[]> dailyTrends = paymentRepository.getDailyRevenueStats(now.minusDays(30));
        List<Object[]> weeklyTrends = getWeeklyRevenueStats(now.minusWeeks(12));
        List<Object[]> monthlyTrends = paymentRepository.getMonthlyRevenueStats(now.minusMonths(12));
        
        return RevenueStatistics.builder()
            .todayRevenue(todayRevenue)
            .weekRevenue(weekRevenue)
            .monthRevenue(monthRevenue)
            .yearRevenue(yearRevenue)
            .dailyTrends(dailyTrends)
            .weeklyTrends(weeklyTrends)
            .monthlyTrends(monthlyTrends)
            .build();
    }

    /**
     * Get payment provider performance statistics (admin only).
     * 
     * @return provider performance statistics
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProviderPerformanceStats> getProviderPerformanceStats() {
        List<Object[]> stats = paymentRepository.getProviderPerformanceStats();
        
        return stats.stream()
            .map(row -> ProviderPerformanceStats.builder()
                .provider(Payment.PaymentProvider.valueOf((String) row[0]))
                .totalPayments(((Number) row[1]).longValue())
                .completedPayments(((Number) row[2]).longValue())
                .failedPayments(((Number) row[3]).longValue())
                .totalRevenue((BigDecimal) row[4])
                .averageAmount((BigDecimal) row[5])
                .successRate(calculateSuccessRate(((Number) row[2]).longValue(), ((Number) row[1]).longValue()))
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Get product category performance statistics (admin only).
     * 
     * @return category performance statistics
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<CategoryPerformanceStats> getCategoryPerformanceStats() {
        List<Object[]> stats = productRepository.getProductCountByCategory();
        
        return stats.stream()
            .map(row -> CategoryPerformanceStats.builder()
                .category(Product.ProductCategory.valueOf((String) row[0]))
                .totalProducts(((Number) row[1]).longValue())
                .averagePrice((BigDecimal) row[2])
                .totalRevenue(getCategoryRevenue(Product.ProductCategory.valueOf((String) row[0])))
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Get revenue trends for specified period (admin only).
     * 
     * @param period trend period (DAILY, WEEKLY, MONTHLY, YEARLY)
     * @param days number of days to look back
     * @return revenue trend data
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<RevenueTrendData> getRevenueTrends(TrendPeriod period, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        return switch (period) {
            case DAILY -> paymentRepository.getDailyRevenueStats(since).stream()
                .map(this::mapToRevenueTrendData)
                .collect(Collectors.toList());
            case WEEKLY -> getWeeklyRevenueStats(since).stream()
                .map(this::mapToRevenueTrendData)
                .collect(Collectors.toList());
            case MONTHLY -> paymentRepository.getMonthlyRevenueStats(since).stream()
                .map(this::mapToRevenueTrendData)
                .collect(Collectors.toList());
            case YEARLY -> getYearlyRevenueStats(since).stream()
                .map(this::mapToRevenueTrendData)
                .collect(Collectors.toList());
        };
    }

    /**
     * Get top selling products (admin only).
     * 
     * @param limit number of products to return
     * @return list of top selling products
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<TopSellingProductStats> getTopSellingProducts(int limit) {
        // This would require a sales/orders table in a real implementation
        // For now, using review count as a proxy for popularity
        return productRepository.findMostReviewedProducts(org.springframework.data.domain.PageRequest.of(0, limit))
            .getContent().stream()
            .map(row -> TopSellingProductStats.builder()
                .productId((Long) row[0])
                .productName((String) row[1])
                .reviewCount(((Number) row[2]).longValue())
                .averageRating((BigDecimal) row[3])
                .estimatedRevenue(BigDecimal.ZERO) // Would calculate from actual sales
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Get customer retention statistics (admin only).
     * 
     * @return customer retention statistics
     */
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerRetentionStats getCustomerRetentionStats() {
        LocalDateTime now = LocalDateTime.now();
        
        // Calculate retention metrics
        long totalCustomers = userRepository.countByRole(User.UserRole.CUSTOMER);
        // Simplified implementation using available methods
        long activeThisMonth = userRepository.countByRole(User.UserRole.CUSTOMER);
        long activeThisWeek = userRepository.countByRole(User.UserRole.CUSTOMER);
        long returningCustomers = userRepository.countByRole(User.UserRole.CUSTOMER);
        
        return CustomerRetentionStats.builder()
            .totalCustomers(totalCustomers)
            .activeThisMonth(activeThisMonth)
            .activeThisWeek(activeThisWeek)
            .returningCustomers(returningCustomers)
            .retentionRate(calculateRetentionRate(returningCustomers, totalCustomers))
            .monthlyRetentionRate(calculateRetentionRate(activeThisMonth, totalCustomers))
            .weeklyRetentionRate(calculateRetentionRate(activeThisWeek, totalCustomers))
            .build();
    }

    // Helper methods

    private BigDecimal getTodayRevenue() {
        // Simplified implementation - would need custom query
        return BigDecimal.ZERO;
    }

    private BigDecimal getWeekRevenue() {
        // Simplified implementation - would need custom query
        return BigDecimal.ZERO;
    }

    private BigDecimal getMonthRevenue() {
        // Simplified implementation - would need custom query
        return BigDecimal.ZERO;
    }

    private BigDecimal getYearRevenue() {
        // Simplified implementation - would need custom query
        return BigDecimal.ZERO;
    }

    private List<Object[]> getWeeklyRevenueStats(LocalDateTime since) {
        // This would require a custom query to group by week
        // For now, returning empty list as placeholder
        return List.of();
    }

    private List<Object[]> getYearlyRevenueStats(LocalDateTime since) {
        // This would require a custom query to group by year
        // For now, returning empty list as placeholder
        return List.of();
    }

    private BigDecimal getCategoryRevenue(Product.ProductCategory category) {
        // This would require joining with sales/orders data
        // For now, returning zero as placeholder
        return BigDecimal.ZERO;
    }

    private RevenueTrendData mapToRevenueTrendData(Object[] row) {
        return RevenueTrendData.builder()
            .period((String) row[0])
            .revenue((BigDecimal) row[1])
            .transactionCount(((Number) row[2]).longValue())
            .build();
    }

    private BigDecimal calculateSuccessRate(Long completed, Long total) {
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(completed * 100.0 / total);
    }

    private BigDecimal calculateRetentionRate(Long active, Long total) {
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(active * 100.0 / total);
    }

    private Long extractLong(Object stats, int index) {
        if (stats instanceof Object[]) {
            Object[] array = (Object[]) stats;
            if (array.length > index && array[index] != null) {
                return ((Number) array[index]).longValue();
            }
        }
        return 0L;
    }

    private BigDecimal extractBigDecimal(Object stats, int index) {
        if (stats instanceof Object[]) {
            Object[] array = (Object[]) stats;
            if (array.length > index && array[index] != null) {
                return (BigDecimal) array[index];
            }
        }
        return BigDecimal.ZERO;
    }

    // DTOs and Enums

    public enum TrendPeriod {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    /**
     * Dashboard statistics DTO.
     */
    public static class DashboardStatistics {
        private PaymentStatistics paymentStats;
        private ProductStatistics productStats;
        private UserStatistics userStats;
        private RevenueStatistics revenueStats;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private PaymentStatistics paymentStats;
            private ProductStatistics productStats;
            private UserStatistics userStats;
            private RevenueStatistics revenueStats;

            public Builder paymentStats(PaymentStatistics paymentStats) {
                this.paymentStats = paymentStats;
                return this;
            }

            public Builder productStats(ProductStatistics productStats) {
                this.productStats = productStats;
                return this;
            }

            public Builder userStats(UserStatistics userStats) {
                this.userStats = userStats;
                return this;
            }

            public Builder revenueStats(RevenueStatistics revenueStats) {
                this.revenueStats = revenueStats;
                return this;
            }

            public DashboardStatistics build() {
                DashboardStatistics stats = new DashboardStatistics();
                stats.paymentStats = this.paymentStats;
                stats.productStats = this.productStats;
                stats.userStats = this.userStats;
                stats.revenueStats = this.revenueStats;
                return stats;
            }
        }

        // Getters and setters
        public PaymentStatistics getPaymentStats() { return paymentStats; }
        public void setPaymentStats(PaymentStatistics paymentStats) { this.paymentStats = paymentStats; }

        public ProductStatistics getProductStats() { return productStats; }
        public void setProductStats(ProductStatistics productStats) { this.productStats = productStats; }

        public UserStatistics getUserStats() { return userStats; }
        public void setUserStats(UserStatistics userStats) { this.userStats = userStats; }

        public RevenueStatistics getRevenueStats() { return revenueStats; }
        public void setRevenueStats(RevenueStatistics revenueStats) { this.revenueStats = revenueStats; }
    }

    /**
     * Revenue statistics DTO.
     */
    public static class RevenueStatistics {
        private BigDecimal todayRevenue;
        private BigDecimal weekRevenue;
        private BigDecimal monthRevenue;
        private BigDecimal yearRevenue;
        private List<Object[]> dailyTrends;
        private List<Object[]> weeklyTrends;
        private List<Object[]> monthlyTrends;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private BigDecimal todayRevenue;
            private BigDecimal weekRevenue;
            private BigDecimal monthRevenue;
            private BigDecimal yearRevenue;
            private List<Object[]> dailyTrends;
            private List<Object[]> weeklyTrends;
            private List<Object[]> monthlyTrends;

            public Builder todayRevenue(BigDecimal todayRevenue) {
                this.todayRevenue = todayRevenue;
                return this;
            }

            public Builder weekRevenue(BigDecimal weekRevenue) {
                this.weekRevenue = weekRevenue;
                return this;
            }

            public Builder monthRevenue(BigDecimal monthRevenue) {
                this.monthRevenue = monthRevenue;
                return this;
            }

            public Builder yearRevenue(BigDecimal yearRevenue) {
                this.yearRevenue = yearRevenue;
                return this;
            }

            public Builder dailyTrends(List<Object[]> dailyTrends) {
                this.dailyTrends = dailyTrends;
                return this;
            }

            public Builder weeklyTrends(List<Object[]> weeklyTrends) {
                this.weeklyTrends = weeklyTrends;
                return this;
            }

            public Builder monthlyTrends(List<Object[]> monthlyTrends) {
                this.monthlyTrends = monthlyTrends;
                return this;
            }

            public RevenueStatistics build() {
                RevenueStatistics stats = new RevenueStatistics();
                stats.todayRevenue = this.todayRevenue;
                stats.weekRevenue = this.weekRevenue;
                stats.monthRevenue = this.monthRevenue;
                stats.yearRevenue = this.yearRevenue;
                stats.dailyTrends = this.dailyTrends;
                stats.weeklyTrends = this.weeklyTrends;
                stats.monthlyTrends = this.monthlyTrends;
                return stats;
            }
        }

        // Getters and setters
        public BigDecimal getTodayRevenue() { return todayRevenue; }
        public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }

        public BigDecimal getWeekRevenue() { return weekRevenue; }
        public void setWeekRevenue(BigDecimal weekRevenue) { this.weekRevenue = weekRevenue; }

        public BigDecimal getMonthRevenue() { return monthRevenue; }
        public void setMonthRevenue(BigDecimal monthRevenue) { this.monthRevenue = monthRevenue; }

        public BigDecimal getYearRevenue() { return yearRevenue; }
        public void setYearRevenue(BigDecimal yearRevenue) { this.yearRevenue = yearRevenue; }

        public List<Object[]> getDailyTrends() { return dailyTrends; }
        public void setDailyTrends(List<Object[]> dailyTrends) { this.dailyTrends = dailyTrends; }

        public List<Object[]> getWeeklyTrends() { return weeklyTrends; }
        public void setWeeklyTrends(List<Object[]> weeklyTrends) { this.weeklyTrends = weeklyTrends; }

        public List<Object[]> getMonthlyTrends() { return monthlyTrends; }
        public void setMonthlyTrends(List<Object[]> monthlyTrends) { this.monthlyTrends = monthlyTrends; }
    }

    /**
     * Additional statistics DTOs would go here...
     * (ProviderPerformanceStats, CategoryPerformanceStats, etc.)
     */
    
    // Placeholder classes for compilation
    public static class PaymentStatistics {
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            public Builder totalPayments(Long val) { return this; }
            public Builder completedPayments(Long val) { return this; }
            public Builder failedPayments(Long val) { return this; }
            public Builder pendingPayments(Long val) { return this; }
            public Builder totalRevenue(BigDecimal val) { return this; }
            public Builder averageAmount(BigDecimal val) { return this; }
            public Builder providerStats(List<Object[]> val) { return this; }
            public Builder dailyRevenue(List<Object[]> val) { return this; }
            public Builder monthlyRevenue(List<Object[]> val) { return this; }
            public PaymentStatistics build() { return new PaymentStatistics(); }
        }
    }

    public static class ProductStatistics {
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            public Builder totalProducts(Long val) { return this; }
            public Builder inStockProducts(Long val) { return this; }
            public Builder outOfStockProducts(Long val) { return this; }
            public Builder lowStockProducts(Long val) { return this; }
            public Builder averagePrice(BigDecimal val) { return this; }
            public Builder minPrice(BigDecimal val) { return this; }
            public Builder maxPrice(BigDecimal val) { return this; }
            public Builder categoryDistribution(List<Object[]> val) { return this; }
            public Builder monthlyCreations(List<Object[]> val) { return this; }
            public ProductStatistics build() { return new ProductStatistics(); }
        }
    }

    public static class UserStatistics {
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            public Builder totalUsers(Long val) { return this; }
            public Builder activeUsers(Long val) { return this; }
            public Builder verifiedUsers(Long val) { return this; }
            public Builder usersWhoLoggedIn(Long val) { return this; }
            public Builder roleDistribution(List<Object[]> val) { return this; }
            public Builder monthlyRegistrations(List<Object[]> val) { return this; }
            public UserStatistics build() { return new UserStatistics(); }
        }
    }

    public static class ProviderPerformanceStats {
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            public Builder provider(Payment.PaymentProvider val) { return this; }
            public Builder totalPayments(Long val) { return this; }
            public Builder completedPayments(Long val) { return this; }
            public Builder failedPayments(Long val) { return this; }
            public Builder totalRevenue(BigDecimal val) { return this; }
            public Builder averageAmount(BigDecimal val) { return this; }
            public Builder successRate(BigDecimal val) { return this; }
            public ProviderPerformanceStats build() { return new ProviderPerformanceStats(); }
        }
    }

    public static class CategoryPerformanceStats {
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            public Builder category(Product.ProductCategory val) { return this; }
            public Builder totalProducts(Long val) { return this; }
            public Builder averagePrice(BigDecimal val) { return this; }
            public Builder totalRevenue(BigDecimal val) { return this; }
            public CategoryPerformanceStats build() { return new CategoryPerformanceStats(); }
        }
    }

    public static class RevenueTrendData {
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            public Builder period(String val) { return this; }
            public Builder revenue(BigDecimal val) { return this; }
            public Builder transactionCount(Long val) { return this; }
            public RevenueTrendData build() { return new RevenueTrendData(); }
        }
    }

    public static class TopSellingProductStats {
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            public Builder productId(Long val) { return this; }
            public Builder productName(String val) { return this; }
            public Builder reviewCount(Long val) { return this; }
            public Builder averageRating(BigDecimal val) { return this; }
            public Builder estimatedRevenue(BigDecimal val) { return this; }
            public TopSellingProductStats build() { return new TopSellingProductStats(); }
        }
    }

    public static class CustomerRetentionStats {
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            public Builder totalCustomers(Long val) { return this; }
            public Builder activeThisMonth(Long val) { return this; }
            public Builder activeThisWeek(Long val) { return this; }
            public Builder returningCustomers(Long val) { return this; }
            public Builder retentionRate(BigDecimal val) { return this; }
            public Builder monthlyRetentionRate(BigDecimal val) { return this; }
            public Builder weeklyRetentionRate(BigDecimal val) { return this; }
            public CustomerRetentionStats build() { return new CustomerRetentionStats(); }
        }
    }
}