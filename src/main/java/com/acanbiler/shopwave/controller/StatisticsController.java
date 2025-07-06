package com.acanbiler.shopwave.controller;

import com.acanbiler.shopwave.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Statistics and analytics controller for ShopWave application.
 * 
 * This controller provides comprehensive business analytics, metrics,
 * and reporting capabilities for administrators with proper security controls.
 */
@RestController
@RequestMapping("/statistics")
@Tag(name = "Statistics & Analytics", description = "Business analytics and reporting operations")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * Get comprehensive dashboard statistics.
     */
    @Operation(
        summary = "Get dashboard statistics",
        description = "Retrieves comprehensive dashboard statistics including payment, product, user, and revenue metrics. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticsService.DashboardStatistics.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<StatisticsService.DashboardStatistics> getDashboardStatistics() {
        
        StatisticsService.DashboardStatistics statistics = statisticsService.getDashboardStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get payment statistics.
     */
    @Operation(
        summary = "Get payment statistics",
        description = "Retrieves detailed payment analytics including transaction counts, revenue, and provider performance. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticsService.PaymentStatistics.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/payments")
    public ResponseEntity<StatisticsService.PaymentStatistics> getPaymentStatistics() {
        
        StatisticsService.PaymentStatistics statistics = statisticsService.getPaymentStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get product statistics.
     */
    @Operation(
        summary = "Get product statistics",
        description = "Retrieves detailed product analytics including inventory levels, pricing, and category distribution. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticsService.ProductStatistics.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/products")
    public ResponseEntity<StatisticsService.ProductStatistics> getProductStatistics() {
        
        StatisticsService.ProductStatistics statistics = statisticsService.getProductStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get user statistics.
     */
    @Operation(
        summary = "Get user statistics",
        description = "Retrieves detailed user analytics including registration trends, role distribution, and activity metrics. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticsService.UserStatistics.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/users")
    public ResponseEntity<StatisticsService.UserStatistics> getUserStatistics() {
        
        StatisticsService.UserStatistics statistics = statisticsService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get revenue statistics.
     */
    @Operation(
        summary = "Get revenue statistics",
        description = "Retrieves detailed revenue analytics including daily, weekly, monthly, and yearly trends. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Revenue statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticsService.RevenueStatistics.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/revenue")
    public ResponseEntity<StatisticsService.RevenueStatistics> getRevenueStatistics() {
        
        StatisticsService.RevenueStatistics statistics = statisticsService.getRevenueStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get payment provider performance statistics.
     */
    @Operation(
        summary = "Get provider performance statistics",
        description = "Retrieves performance metrics for each payment provider including success rates and revenue contribution. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Provider performance statistics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/providers")
    public ResponseEntity<List<StatisticsService.ProviderPerformanceStats>> getProviderPerformanceStats() {
        
        List<StatisticsService.ProviderPerformanceStats> statistics = statisticsService.getProviderPerformanceStats();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get category performance statistics.
     */
    @Operation(
        summary = "Get category performance statistics",
        description = "Retrieves performance metrics for each product category including product counts and revenue. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category performance statistics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/categories")
    public ResponseEntity<List<StatisticsService.CategoryPerformanceStats>> getCategoryPerformanceStats() {
        
        List<StatisticsService.CategoryPerformanceStats> statistics = statisticsService.getCategoryPerformanceStats();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get revenue trends.
     */
    @Operation(
        summary = "Get revenue trends",
        description = "Retrieves revenue trend data for specified period and timeframe. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Revenue trends retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid period or days parameter"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/revenue/trends")
    public ResponseEntity<List<StatisticsService.RevenueTrendData>> getRevenueTrends(
        @Parameter(description = "Trend period", example = "DAILY")
        @RequestParam(defaultValue = "DAILY") StatisticsService.TrendPeriod period,
        @Parameter(description = "Number of days to look back", example = "30")
        @RequestParam(defaultValue = "30") int days) {
        
        List<StatisticsService.RevenueTrendData> trends = statisticsService.getRevenueTrends(period, days);
        return ResponseEntity.ok(trends);
    }

    /**
     * Get top selling products.
     */
    @Operation(
        summary = "Get top selling products",
        description = "Retrieves list of best-performing products based on sales metrics. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Top selling products retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid limit parameter"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/products/top-selling")
    public ResponseEntity<List<StatisticsService.TopSellingProductStats>> getTopSellingProducts(
        @Parameter(description = "Number of products to return", example = "10")
        @RequestParam(defaultValue = "10") int limit) {
        
        List<StatisticsService.TopSellingProductStats> products = statisticsService.getTopSellingProducts(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * Get customer retention statistics.
     */
    @Operation(
        summary = "Get customer retention statistics",
        description = "Retrieves customer retention and activity metrics. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer retention statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticsService.CustomerRetentionStats.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/customers/retention")
    public ResponseEntity<StatisticsService.CustomerRetentionStats> getCustomerRetentionStats() {
        
        StatisticsService.CustomerRetentionStats statistics = statisticsService.getCustomerRetentionStats();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Export statistics data.
     */
    @Operation(
        summary = "Export statistics data",
        description = "Exports comprehensive statistics data in specified format. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics exported successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid export format"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/export")
    public ResponseEntity<ExportResponse> exportStatistics(
        @Parameter(description = "Export format", example = "JSON")
        @RequestParam(defaultValue = "JSON") ExportFormat format,
        @Parameter(description = "Include detailed breakdowns", example = "true")
        @RequestParam(defaultValue = "false") boolean includeDetails) {
        
        // This would typically generate and return export data
        // For now, returning a simple response
        ExportResponse response = new ExportResponse(
            "Statistics export completed",
            format.name(),
            includeDetails
        );
        
        return ResponseEntity.ok(response);
    }

    // Enums and DTOs

    /**
     * Export format enumeration.
     */
    public enum ExportFormat {
        JSON, CSV, EXCEL, PDF
    }

    /**
     * Export response DTO.
     */
    @Schema(description = "Statistics export response")
    public static class ExportResponse {
        
        @Schema(description = "Export message", example = "Statistics export completed")
        private String message;

        @Schema(description = "Export format", example = "JSON")
        private String format;

        @Schema(description = "Includes detailed data", example = "true")
        private boolean includeDetails;

        public ExportResponse(String message, String format, boolean includeDetails) {
            this.message = message;
            this.format = format;
            this.includeDetails = includeDetails;
        }

        // Getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }

        public boolean isIncludeDetails() { return includeDetails; }
        public void setIncludeDetails(boolean includeDetails) { this.includeDetails = includeDetails; }
    }
}