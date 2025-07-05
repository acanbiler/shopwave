package com.acanbiler.shopwave.controller;

import com.acanbiler.shopwave.entity.Payment;
import com.acanbiler.shopwave.service.PaymentService;
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
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment processing controller for ShopWave application.
 * 
 * This controller handles payment operations, webhook processing,
 * refunds, and payment analytics with proper security controls.
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Management", description = "Payment processing and transaction management operations")
@SecurityRequirement(name = "Bearer Authentication")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Get all payments (admin only).
     */
    @Operation(
        summary = "Get all payments",
        description = "Retrieves paginated list of all payments. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentPageResponse> getAllPayments(
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Payment> payments = paymentService.findAll(pageable);
        PaymentPageResponse response = PaymentPageResponse.fromPage(payments);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get payments by user.
     */
    @Operation(
        summary = "Get user payments",
        description = "Retrieves payments for a specific user. Users can access their own payments, admins can access any user's payments."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User payments retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<PaymentPageResponse> getUserPayments(
        @Parameter(description = "User ID", example = "123", required = true)
        @PathVariable Long userId,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Payment> payments = paymentService.findByUser(userId, pageable);
        PaymentPageResponse response = PaymentPageResponse.fromPage(payments);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get payments by status (admin only).
     */
    @Operation(
        summary = "Get payments by status",
        description = "Retrieves payments filtered by status. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentPageResponse> getPaymentsByStatus(
        @Parameter(description = "Payment status", example = "COMPLETED", required = true)
        @PathVariable Payment.PaymentStatus status,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Payment> payments = paymentService.findByStatus(status, pageable);
        PaymentPageResponse response = PaymentPageResponse.fromPage(payments);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get payments by provider (admin only).
     */
    @Operation(
        summary = "Get payments by provider",
        description = "Retrieves payments filtered by payment provider. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "400", description = "Invalid provider"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/provider/{provider}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentPageResponse> getPaymentsByProvider(
        @Parameter(description = "Payment provider", example = "IYZICO", required = true)
        @PathVariable Payment.PaymentProvider provider,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Payment> payments = paymentService.findByProvider(provider, pageable);
        PaymentPageResponse response = PaymentPageResponse.fromPage(payments);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by ID.
     */
    @Operation(
        summary = "Get payment by ID",
        description = "Retrieves payment details by ID. Users can access their own payments, admins can access any payment."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
        @Parameter(description = "Payment ID", example = "456", required = true)
        @PathVariable Long id) {
        
        Payment payment = paymentService.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
            
        PaymentResponse response = PaymentResponse.fromPayment(payment);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by reference number (admin only).
     */
    @Operation(
        summary = "Get payment by reference number",
        description = "Retrieves payment by reference number. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/reference/{referenceNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentByReferenceNumber(
        @Parameter(description = "Payment reference number", example = "PAY-ABC123DEF456", required = true)
        @PathVariable String referenceNumber) {
        
        Payment payment = paymentService.findByReferenceNumber(referenceNumber)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
            
        PaymentResponse response = PaymentResponse.fromPayment(payment);
        return ResponseEntity.ok(response);
    }

    /**
     * Process a new payment.
     */
    @Operation(
        summary = "Process payment",
        description = "Processes a new payment transaction using the specified payment method and provider."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment processed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed or payment processing error"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
        @Parameter(description = "Payment processing request", required = true)
        @Valid @RequestBody PaymentProcessRequest request) {
        
        Payment payment = paymentService.processPayment(request.toServiceRequest());
        PaymentResponse response = PaymentResponse.fromPayment(payment);
        
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Process payment webhook.
     */
    @Operation(
        summary = "Process payment webhook",
        description = "Processes webhook notifications from payment providers to update payment status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid webhook signature or data"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/webhook/{provider}")
    public ResponseEntity<PaymentResponse> processWebhook(
        @Parameter(description = "Payment provider", example = "IYZICO", required = true)
        @PathVariable Payment.PaymentProvider provider,
        @Parameter(description = "Webhook signature", required = true)
        @RequestHeader("X-Webhook-Signature") String signature,
        @Parameter(description = "Webhook payload", required = true)
        @RequestBody String webhookData) {
        
        Payment payment = paymentService.processWebhook(webhookData, signature, provider);
        PaymentResponse response = PaymentResponse.fromPayment(payment);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Refund payment (admin only).
     */
    @Operation(
        summary = "Refund payment",
        description = "Processes a refund for a completed payment. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund processed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid refund request or payment cannot be refunded"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> refundPayment(
        @Parameter(description = "Payment ID", example = "456", required = true)
        @PathVariable Long id,
        @Parameter(description = "Refund request details", required = true)
        @Valid @RequestBody RefundRequest request) {
        
        Payment payment = paymentService.refundPayment(id, request.getAmount(), request.getReason());
        PaymentResponse response = PaymentResponse.fromPayment(payment);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent transactions (admin only).
     */
    @Operation(
        summary = "Get recent transactions",
        description = "Retrieves recent payment transactions within specified number of days. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent transactions retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentPageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentPageResponse> getRecentTransactions(
        @Parameter(description = "Number of days to look back", example = "7")
        @RequestParam(defaultValue = "7") int days,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Payment> payments = paymentService.getRecentTransactions(days, pageable);
        PaymentPageResponse response = PaymentPageResponse.fromPage(payments);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get failed payments for retry (admin only).
     */
    @Operation(
        summary = "Get failed payments for retry",
        description = "Retrieves failed payments that can be retried. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Failed payments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getFailedPayments(
        @Parameter(description = "Number of hours to look back", example = "24")
        @RequestParam(defaultValue = "24") int hours) {
        
        List<Payment> payments = paymentService.getFailedPaymentsForRetry(hours);
        List<PaymentResponse> response = payments.stream()
            .map(PaymentResponse::fromPayment)
            .toList();
            
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment statistics (admin only).
     */
    @Operation(
        summary = "Get payment statistics",
        description = "Retrieves comprehensive payment statistics and analytics. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentService.PaymentStatistics.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentService.PaymentStatistics> getPaymentStatistics() {
        
        PaymentService.PaymentStatistics statistics = paymentService.getPaymentStatistics();
        return ResponseEntity.ok(statistics);
    }

    // DTOs for API requests and responses

    /**
     * Payment processing request DTO.
     */
    @Schema(description = "Payment processing request containing payment details")
    public static class PaymentProcessRequest {
        
        @Schema(description = "User ID", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "User ID is required")
        private Long userId;

        @Schema(description = "Payment amount", example = "99.99", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        private BigDecimal amount;

        @Schema(description = "Currency code", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Currency is required")
        private String currency;

        @Schema(description = "Payment provider", example = "IYZICO", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Provider is required")
        private Payment.PaymentProvider provider;

        @Schema(description = "Payment method", example = "CREDIT_CARD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Payment method is required")
        private Payment.PaymentMethod paymentMethod;

        @Schema(description = "Payment description", example = "Order payment for items")
        private String description;

        @Schema(description = "Credit card number", example = "4111111111111111")
        private String cardNumber;

        @Schema(description = "Card expiry month", example = "12")
        private String expiryMonth;

        @Schema(description = "Card expiry year", example = "2025")
        private String expiryYear;

        @Schema(description = "Card CVC", example = "123")
        private String cvc;

        @Schema(description = "Card holder name", example = "John Doe")
        private String cardHolderName;

        // Constructors
        public PaymentProcessRequest() {}

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public Payment.PaymentProvider getProvider() { return provider; }
        public void setProvider(Payment.PaymentProvider provider) { this.provider = provider; }

        public Payment.PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(Payment.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

        public String getExpiryMonth() { return expiryMonth; }
        public void setExpiryMonth(String expiryMonth) { this.expiryMonth = expiryMonth; }

        public String getExpiryYear() { return expiryYear; }
        public void setExpiryYear(String expiryYear) { this.expiryYear = expiryYear; }

        public String getCvc() { return cvc; }
        public void setCvc(String cvc) { this.cvc = cvc; }

        public String getCardHolderName() { return cardHolderName; }
        public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

        public PaymentService.PaymentProcessRequest toServiceRequest() {
            PaymentService.PaymentProcessRequest request = new PaymentService.PaymentProcessRequest();
            request.setUserId(this.userId);
            request.setAmount(this.amount);
            request.setCurrency(this.currency);
            request.setProvider(this.provider);
            request.setPaymentMethod(this.paymentMethod);
            request.setDescription(this.description);
            request.setCardNumber(this.cardNumber);
            request.setExpiryMonth(this.expiryMonth);
            request.setExpiryYear(this.expiryYear);
            request.setCvc(this.cvc);
            request.setCardHolderName(this.cardHolderName);
            return request;
        }
    }

    /**
     * Refund request DTO.
     */
    @Schema(description = "Refund request for processing payment refunds")
    public static class RefundRequest {
        
        @Schema(description = "Refund amount", example = "49.99", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Refund amount is required")
        @Positive(message = "Refund amount must be positive")
        private BigDecimal amount;

        @Schema(description = "Refund reason", example = "Customer requested refund", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Refund reason is required")
        private String reason;

        // Constructors
        public RefundRequest() {}

        // Getters and setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * Payment response DTO.
     */
    @Schema(description = "Payment information response")
    public static class PaymentResponse {
        
        @Schema(description = "Payment unique identifier", example = "456")
        private Long id;

        @Schema(description = "Payment amount", example = "99.99")
        private BigDecimal amount;

        @Schema(description = "Currency code", example = "USD")
        private String currency;

        @Schema(description = "Payment provider", example = "IYZICO")
        private String provider;

        @Schema(description = "Payment method", example = "CREDIT_CARD")
        private String paymentMethod;

        @Schema(description = "Payment status", example = "COMPLETED")
        private String status;

        @Schema(description = "Payment reference number", example = "PAY-ABC123DEF456")
        private String referenceNumber;

        @Schema(description = "Provider payment ID", example = "PROV-XYZ789")
        private String providerPaymentId;

        @Schema(description = "Payment description", example = "Order payment for items")
        private String description;

        @Schema(description = "User ID", example = "123")
        private Long userId;

        @Schema(description = "User email", example = "user@example.com")
        private String userEmail;

        @Schema(description = "Net amount after fees", example = "97.50")
        private BigDecimal netAmount;

        @Schema(description = "Dispute amount", example = "0.00")
        private BigDecimal disputeAmount;

        @Schema(description = "Refund amount", example = "0.00")
        private BigDecimal refundAmount;

        @Schema(description = "Card last four digits", example = "1234")
        private String cardLastFour;

        @Schema(description = "Card brand", example = "VISA")
        private String cardBrand;

        @Schema(description = "Failure reason", example = "Insufficient funds")
        private String failureReason;

        @Schema(description = "Payment successful status", example = "true")
        private Boolean successful;

        @Schema(description = "Refunded status", example = "false")
        private Boolean refunded;

        @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "Processing timestamp", example = "2024-01-15T10:30:05")
        private LocalDateTime processedAt;

        @Schema(description = "Failure timestamp", example = "2024-01-15T10:30:03")
        private LocalDateTime failedAt;

        @Schema(description = "Refund timestamp", example = "2024-01-15T12:00:00")
        private LocalDateTime refundedAt;

        public static PaymentResponse fromPayment(Payment payment) {
            PaymentResponse response = new PaymentResponse();
            response.id = payment.getId();
            response.amount = payment.getAmount();
            response.currency = payment.getCurrency();
            response.provider = payment.getProvider().name();
            response.paymentMethod = payment.getPaymentMethod().name();
            response.status = payment.getStatus().name();
            response.referenceNumber = payment.getReferenceNumber();
            response.providerPaymentId = payment.getProviderPaymentId();
            response.description = payment.getDescription();
            response.userId = payment.getUser().getId();
            response.userEmail = payment.getUser().getEmail();
            response.netAmount = payment.getNetAmount();
            response.disputeAmount = payment.getDisputeAmount();
            response.refundAmount = payment.getRefundAmount();
            response.cardLastFour = payment.getCardLastFour();
            response.cardBrand = payment.getCardBrand();
            response.failureReason = payment.getFailureReason();
            response.successful = payment.isSuccessful();
            response.refunded = payment.isRefunded();
            response.createdAt = payment.getCreatedAt();
            response.processedAt = payment.getProcessedAt();
            response.failedAt = payment.getFailedAt();
            response.refundedAt = payment.getRefundedAt();
            return response;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

        public String getProviderPaymentId() { return providerPaymentId; }
        public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public BigDecimal getNetAmount() { return netAmount; }
        public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

        public BigDecimal getDisputeAmount() { return disputeAmount; }
        public void setDisputeAmount(BigDecimal disputeAmount) { this.disputeAmount = disputeAmount; }

        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

        public String getCardLastFour() { return cardLastFour; }
        public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }

        public String getCardBrand() { return cardBrand; }
        public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }

        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

        public Boolean getSuccessful() { return successful; }
        public void setSuccessful(Boolean successful) { this.successful = successful; }

        public Boolean getRefunded() { return refunded; }
        public void setRefunded(Boolean refunded) { this.refunded = refunded; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

        public LocalDateTime getFailedAt() { return failedAt; }
        public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }

        public LocalDateTime getRefundedAt() { return refundedAt; }
        public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }
    }

    /**
     * Paginated payment response DTO.
     */
    @Schema(description = "Paginated payment response")
    public static class PaymentPageResponse {
        
        @Schema(description = "List of payments")
        private List<PaymentResponse> payments;

        @Schema(description = "Current page number", example = "0")
        private int page;

        @Schema(description = "Page size", example = "20")
        private int size;

        @Schema(description = "Total number of elements", example = "1000")
        private long totalElements;

        @Schema(description = "Total number of pages", example = "50")
        private int totalPages;

        @Schema(description = "Is first page", example = "true")
        private boolean first;

        @Schema(description = "Is last page", example = "false")
        private boolean last;

        public static PaymentPageResponse fromPage(Page<Payment> page) {
            PaymentPageResponse response = new PaymentPageResponse();
            response.payments = page.getContent().stream()
                .map(PaymentResponse::fromPayment)
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
        public List<PaymentResponse> getPayments() { return payments; }
        public void setPayments(List<PaymentResponse> payments) { this.payments = payments; }

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
}