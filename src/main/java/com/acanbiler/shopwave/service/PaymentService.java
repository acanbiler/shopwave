package com.acanbiler.shopwave.service;

import com.acanbiler.shopwave.config.PaymentConfig;
import com.acanbiler.shopwave.entity.Payment;
import com.acanbiler.shopwave.entity.User;
import com.acanbiler.shopwave.repository.PaymentRepository;
import com.acanbiler.shopwave.repository.UserRepository;
import com.acanbiler.shopwave.service.payment.PaymentProcessingException;
import com.acanbiler.shopwave.service.payment.PaymentProvider;
import com.acanbiler.shopwave.service.payment.WebhookVerificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment processing service for ShopWave application.
 * 
 * This service handles payment processing, provider abstraction,
 * webhook processing, and payment-related business logic.
 */
@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentConfig.PaymentProviderRegistry providerRegistry;
    private final WebhookVerificationService webhookVerificationService;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository,
                         PaymentConfig.PaymentProviderRegistry providerRegistry,
                         WebhookVerificationService webhookVerificationService) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.providerRegistry = providerRegistry;
        this.webhookVerificationService = webhookVerificationService;
    }

    /**
     * Find payment by ID.
     * 
     * @param id payment ID
     * @return payment if found
     */
    @PreAuthorize("hasRole('ADMIN') or @paymentService.isPaymentOwner(#id, authentication.name)")
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    /**
     * Find payment by reference number.
     * 
     * @param referenceNumber payment reference number
     * @return payment if found
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Payment> findByReferenceNumber(String referenceNumber) {
        return paymentRepository.findByReferenceNumber(referenceNumber);
    }

    /**
     * Get all payments with pagination (admin only).
     * 
     * @param pageable pagination parameters
     * @return page of payments
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Payment> findAll(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    /**
     * Get user payments.
     * 
     * @param userId user ID
     * @param pageable pagination parameters
     * @return page of user payments
     */
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#userId, authentication.name)")
    public Page<Payment> findByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return paymentRepository.findByUser(user, pageable);
    }

    /**
     * Get payments by status (admin only).
     * 
     * @param status payment status
     * @param pageable pagination parameters
     * @return page of payments with specified status
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByStatus(status, pageable);
    }

    /**
     * Get payments by provider (admin only).
     * 
     * @param provider payment provider
     * @param pageable pagination parameters
     * @return page of payments from specified provider
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Payment> findByProvider(Payment.PaymentProvider provider, Pageable pageable) {
        return paymentRepository.findByProvider(provider, pageable);
    }

    /**
     * Process a new payment.
     * 
     * @param paymentRequest payment processing request
     * @return created payment
     */
    public Payment processPayment(PaymentProcessRequest paymentRequest) {
        User user = userRepository.findById(paymentRequest.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate unique reference number
        String referenceNumber = generateReferenceNumber();

        // Create payment record
        Payment payment = Payment.builder()
            .amount(paymentRequest.getAmount())
            .currency(paymentRequest.getCurrency())
            .provider(paymentRequest.getProvider())
            .status(Payment.PaymentStatus.PENDING)
            .referenceNumber(referenceNumber)
            .description(paymentRequest.getDescription())
            .paymentMethod(paymentRequest.getPaymentMethod())
            .user(user)
            .build();

        payment = paymentRepository.save(payment);

        // Process payment with provider
        try {
            PaymentProvider provider = getPaymentProvider(paymentRequest.getProvider());
            if (provider == null) {
                throw new PaymentProcessingException("Payment provider not available: " + paymentRequest.getProvider());
            }

            // Create provider payment request
            PaymentProvider.PaymentRequest providerRequest = createProviderRequest(payment, paymentRequest);
            
            // Process payment
            PaymentProvider.PaymentResponse providerResponse = provider.processPayment(providerRequest);
            
            // Update payment with provider response
            payment.setProviderPaymentId(providerResponse.getProviderPaymentId());
            payment.setStatus(mapProviderStatusToPaymentStatus(providerResponse.getStatus()));
            
            if (providerResponse.isSuccess()) {
                payment.setProcessedAt(LocalDateTime.now());
            } else {
                payment.setFailedAt(LocalDateTime.now());
                payment.setFailureReason(providerResponse.getErrorMessage());
            }

            // Set card information if available
            if (providerResponse.getCardLastFour() != null) {
                payment.setCardLastFour(providerResponse.getCardLastFour());
            }
            if (providerResponse.getCardBrand() != null) {
                payment.setCardBrand(providerResponse.getCardBrand());
            }

            return paymentRepository.save(payment);

        } catch (PaymentProcessingException e) {
            // Mark payment as failed
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailedAt(LocalDateTime.now());
            payment.setFailureReason("Payment processing error: " + e.getMessage());
            return paymentRepository.save(payment);
        }
    }

    /**
     * Process webhook from payment provider.
     * 
     * @param webhookData webhook payload
     * @param signature webhook signature
     * @param provider payment provider
     * @return processed payment
     */
    public Payment processWebhook(String webhookData, String signature, Payment.PaymentProvider provider) {
        try {
            // Verify webhook signature
            if (!webhookVerificationService.verifyWebhookSignature(provider, webhookData, signature)) {
                throw new PaymentProcessingException("Invalid webhook signature");
            }

            // Parse webhook data to extract payment information
            WebhookVerificationService.WebhookPaymentData parsedData = 
                webhookVerificationService.parseWebhookData(provider, webhookData);
            
            // Find payment by provider payment ID or reference number
            Payment payment = paymentRepository.findByProviderPaymentId(parsedData.getProviderPaymentId())
                .or(() -> paymentRepository.findByReferenceNumber(parsedData.getReferenceNumber()))
                .orElseThrow(() -> new PaymentProcessingException("Payment not found"));

            // Update payment status
            payment.setStatus(mapProviderStatusToPaymentStatus(parsedData.getStatus()));
            payment.setWebhookData(webhookData);
            payment.setWebhookReceivedAt(LocalDateTime.now());

            if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
                payment.setProcessedAt(LocalDateTime.now());
            } else if (payment.getStatus() == Payment.PaymentStatus.FAILED) {
                payment.setFailedAt(LocalDateTime.now());
                payment.setFailureReason(parsedData.getErrorMessage());
            }

            return paymentRepository.save(payment);
            
        } catch (PaymentProcessingException e) {
            throw new RuntimeException("Webhook processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Refund a payment (admin only).
     * 
     * @param paymentId payment ID
     * @param refundAmount refund amount
     * @param reason refund reason
     * @return updated payment
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Payment refundPayment(Long paymentId, BigDecimal refundAmount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.isSuccessful()) {
            throw new RuntimeException("Cannot refund unsuccessful payment");
        }

        if (refundAmount.compareTo(payment.getNetAmount()) > 0) {
            throw new RuntimeException("Refund amount cannot exceed net payment amount");
        }

        // Process refund with provider
        try {
            RefundResponse refundResponse = processRefundWithProvider(payment, refundAmount, reason);
            
            if (refundResponse.isSuccessful()) {
                payment.setRefundAmount(payment.getRefundAmount() != null ? 
                    payment.getRefundAmount().add(refundAmount) : refundAmount);
                payment.setRefundedAt(LocalDateTime.now());
                
                // Update status if fully refunded
                if (payment.isFullyRefunded()) {
                    payment.setStatus(Payment.PaymentStatus.REFUNDED);
                } else {
                    payment.setStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
                }
            }

            return paymentRepository.save(payment);

        } catch (Exception e) {
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get recent transactions (admin only).
     * 
     * @param days days back to look
     * @param pageable pagination parameters
     * @return page of recent transactions
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Payment> getRecentTransactions(int days, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return paymentRepository.findRecentPayments(since, pageable);
    }

    /**
     * Get failed payments for retry (admin only).
     * 
     * @param hours hours back to look
     * @return list of failed payments
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<Payment> getFailedPaymentsForRetry(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return paymentRepository.findRecentFailedPayments(since);
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
     * Check if user owns the payment.
     * 
     * @param paymentId payment ID
     * @param userEmail user email
     * @return true if user owns the payment
     */
    public boolean isPaymentOwner(Long paymentId, String userEmail) {
        return paymentRepository.findById(paymentId)
            .map(payment -> payment.getUser().getEmail().equals(userEmail))
            .orElse(false);
    }

    /**
     * Generate unique reference number.
     */
    private String generateReferenceNumber() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * Process payment with provider.
     */
    private PaymentProviderResponse processWithProvider(Payment payment, PaymentProcessRequest request) {
        // This is a placeholder implementation
        // In real implementation, you would integrate with actual payment providers
        
        PaymentProviderResponse response = new PaymentProviderResponse();
        response.setProviderPaymentId("PROV-" + UUID.randomUUID().toString());
        
        // Simulate payment processing
        if (request.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            // Simulate failure for large amounts
            response.setStatus(Payment.PaymentStatus.FAILED);
            response.setFailureReason("Amount exceeds limit");
        } else {
            response.setStatus(Payment.PaymentStatus.COMPLETED);
        }
        
        // Set card info for credit/debit cards
        if (request.getPaymentMethod() == Payment.PaymentMethod.CREDIT_CARD || 
            request.getPaymentMethod() == Payment.PaymentMethod.DEBIT_CARD) {
            response.setCardLastFour("1234");
            response.setCardBrand("VISA");
        }
        
        return response;
    }

    /**
     * Verify webhook signature.
     */
    private boolean verifyWebhookSignature(String webhookData, String signature, Payment.PaymentProvider provider) {
        // Placeholder implementation
        // In real implementation, you would verify the signature using the provider's secret key
        return signature != null && !signature.isEmpty();
    }

    /**
     * Parse webhook data.
     */
    private WebhookData parseWebhookData(String webhookData, Payment.PaymentProvider provider) {
        // Placeholder implementation
        // In real implementation, you would parse the provider-specific webhook format
        
        WebhookData data = new WebhookData();
        data.setProviderPaymentId("PROV-" + UUID.randomUUID().toString());
        data.setStatus(Payment.PaymentStatus.COMPLETED);
        return data;
    }

    /**
     * Get payment provider for specific provider type.
     * 
     * @param providerType payment provider type
     * @return payment provider or null if not found
     */
    private PaymentProvider getPaymentProvider(Payment.PaymentProvider providerType) {
        String providerName = providerType.name();
        return providerRegistry.getProvider(providerName);
    }

    /**
     * Create provider payment request from service request.
     * 
     * @param payment payment entity
     * @param serviceRequest service payment request
     * @return provider payment request
     */
    private PaymentProvider.PaymentRequest createProviderRequest(Payment payment, PaymentProcessRequest serviceRequest) {
        PaymentProvider.PaymentRequest request = new PaymentProvider.PaymentRequest();
        request.setAmount(serviceRequest.getAmount().toString());
        request.setCurrency(serviceRequest.getCurrency());
        request.setPaymentMethod(serviceRequest.getPaymentMethod().name());
        request.setCardNumber(serviceRequest.getCardNumber());
        request.setExpiryMonth(serviceRequest.getExpiryMonth());
        request.setExpiryYear(serviceRequest.getExpiryYear());
        request.setCvc(serviceRequest.getCvc());
        request.setCardHolderName(serviceRequest.getCardHolderName());
        request.setDescription(serviceRequest.getDescription());
        request.setReferenceNumber(payment.getReferenceNumber());

        // Create customer info
        PaymentProvider.CustomerInfo customer = new PaymentProvider.CustomerInfo();
        customer.setId(payment.getUser().getId().toString());
        customer.setName(payment.getUser().getFirstName());
        customer.setSurname(payment.getUser().getLastName());
        customer.setEmail(payment.getUser().getEmail());
        customer.setPhone(payment.getUser().getPhoneNumber());
        request.setCustomer(customer);

        // Create billing address
        PaymentProvider.BillingAddress billingAddress = new PaymentProvider.BillingAddress();
        billingAddress.setContactName(payment.getUser().getFirstName() + " " + payment.getUser().getLastName());
        billingAddress.setCity("Istanbul");
        billingAddress.setCountry("Turkey");
        billingAddress.setAddress("Test Address");
        billingAddress.setZipCode("34732");
        request.setBillingAddress(billingAddress);

        return request;
    }

    /**
     * Map provider status string to payment status enum.
     * 
     * @param providerStatus provider status string
     * @return payment status enum
     */
    private Payment.PaymentStatus mapProviderStatusToPaymentStatus(String providerStatus) {
        if (providerStatus == null) {
            return Payment.PaymentStatus.PENDING;
        }

        return switch (providerStatus.toUpperCase()) {
            case "SUCCESS", "COMPLETED", "PAID" -> Payment.PaymentStatus.COMPLETED;
            case "FAILED", "FAILURE", "ERROR" -> Payment.PaymentStatus.FAILED;
            case "CANCELLED", "CANCELED" -> Payment.PaymentStatus.CANCELLED;
            case "PENDING", "PROCESSING" -> Payment.PaymentStatus.PROCESSING;
            case "REFUNDED" -> Payment.PaymentStatus.REFUNDED;
            case "PARTIALLY_REFUNDED" -> Payment.PaymentStatus.PARTIALLY_REFUNDED;
            case "DISPUTED" -> Payment.PaymentStatus.DISPUTED;
            default -> Payment.PaymentStatus.PENDING;
        };
    }

    /**
     * Process refund with provider.
     */
    private RefundResponse processRefundWithProvider(Payment payment, BigDecimal amount, String reason) {
        try {
            PaymentProvider provider = getPaymentProvider(payment.getProvider());
            if (provider == null) {
                throw new PaymentProcessingException("Payment provider not available: " + payment.getProvider());
            }

            // Create refund request
            PaymentProvider.RefundRequest refundRequest = new PaymentProvider.RefundRequest();
            refundRequest.setProviderPaymentId(payment.getProviderPaymentId());
            refundRequest.setAmount(amount.toString());
            refundRequest.setCurrency(payment.getCurrency());
            refundRequest.setReason(reason);
            refundRequest.setReferenceNumber(payment.getReferenceNumber());

            // Process refund
            PaymentProvider.RefundResponse providerResponse = provider.processRefund(refundRequest);

            // Map to service response
            RefundResponse response = new RefundResponse();
            response.setSuccessful(providerResponse.isSuccess());
            response.setRefundId(providerResponse.getRefundId());
            
            if (!providerResponse.isSuccess()) {
                response.setFailureReason(providerResponse.getErrorMessage());
            }
            
            return response;
            
        } catch (PaymentProcessingException e) {
            RefundResponse response = new RefundResponse();
            response.setSuccessful(false);
            response.setFailureReason("Refund processing failed: " + e.getMessage());
            return response;
        }
    }

    /**
     * Extract long value from statistics object array.
     */
    private Long extractLong(Object stats, int index) {
        if (stats instanceof Object[]) {
            Object[] array = (Object[]) stats;
            if (array.length > index && array[index] != null) {
                return ((Number) array[index]).longValue();
            }
        }
        return 0L;
    }

    /**
     * Extract BigDecimal value from statistics object array.
     */
    private BigDecimal extractBigDecimal(Object stats, int index) {
        if (stats instanceof Object[]) {
            Object[] array = (Object[]) stats;
            if (array.length > index && array[index] != null) {
                return (BigDecimal) array[index];
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Payment processing request DTO.
     */
    public static class PaymentProcessRequest {
        private Long userId;
        private BigDecimal amount;
        private String currency;
        private Payment.PaymentProvider provider;
        private Payment.PaymentMethod paymentMethod;
        private String description;
        private String cardNumber;
        private String expiryMonth;
        private String expiryYear;
        private String cvc;
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
    }

    /**
     * Payment provider response DTO.
     */
    private static class PaymentProviderResponse {
        private String providerPaymentId;
        private Payment.PaymentStatus status;
        private String failureReason;
        private String cardLastFour;
        private String cardBrand;

        // Getters and setters
        public String getProviderPaymentId() { return providerPaymentId; }
        public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }
        
        public Payment.PaymentStatus getStatus() { return status; }
        public void setStatus(Payment.PaymentStatus status) { this.status = status; }
        
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
        
        public String getCardLastFour() { return cardLastFour; }
        public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }
        
        public String getCardBrand() { return cardBrand; }
        public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
    }

    /**
     * Webhook data DTO.
     */
    private static class WebhookData {
        private String providerPaymentId;
        private Payment.PaymentStatus status;
        private String failureReason;

        // Getters and setters
        public String getProviderPaymentId() { return providerPaymentId; }
        public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }
        
        public Payment.PaymentStatus getStatus() { return status; }
        public void setStatus(Payment.PaymentStatus status) { this.status = status; }
        
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    }

    /**
     * Refund response DTO.
     */
    private static class RefundResponse {
        private boolean successful;
        private String refundId;
        private String failureReason;

        // Getters and setters
        public boolean isSuccessful() { return successful; }
        public void setSuccessful(boolean successful) { this.successful = successful; }
        
        public String getRefundId() { return refundId; }
        public void setRefundId(String refundId) { this.refundId = refundId; }
        
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    }

    /**
     * Payment statistics DTO.
     */
    public static class PaymentStatistics {
        private Long totalPayments;
        private Long completedPayments;
        private Long failedPayments;
        private Long pendingPayments;
        private BigDecimal totalRevenue;
        private BigDecimal averageAmount;
        private List<Object[]> providerStats;
        private List<Object[]> dailyRevenue;
        private List<Object[]> monthlyRevenue;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long totalPayments;
            private Long completedPayments;
            private Long failedPayments;
            private Long pendingPayments;
            private BigDecimal totalRevenue;
            private BigDecimal averageAmount;
            private List<Object[]> providerStats;
            private List<Object[]> dailyRevenue;
            private List<Object[]> monthlyRevenue;

            public Builder totalPayments(Long totalPayments) {
                this.totalPayments = totalPayments;
                return this;
            }

            public Builder completedPayments(Long completedPayments) {
                this.completedPayments = completedPayments;
                return this;
            }

            public Builder failedPayments(Long failedPayments) {
                this.failedPayments = failedPayments;
                return this;
            }

            public Builder pendingPayments(Long pendingPayments) {
                this.pendingPayments = pendingPayments;
                return this;
            }

            public Builder totalRevenue(BigDecimal totalRevenue) {
                this.totalRevenue = totalRevenue;
                return this;
            }

            public Builder averageAmount(BigDecimal averageAmount) {
                this.averageAmount = averageAmount;
                return this;
            }

            public Builder providerStats(List<Object[]> providerStats) {
                this.providerStats = providerStats;
                return this;
            }

            public Builder dailyRevenue(List<Object[]> dailyRevenue) {
                this.dailyRevenue = dailyRevenue;
                return this;
            }

            public Builder monthlyRevenue(List<Object[]> monthlyRevenue) {
                this.monthlyRevenue = monthlyRevenue;
                return this;
            }

            public PaymentStatistics build() {
                PaymentStatistics stats = new PaymentStatistics();
                stats.totalPayments = this.totalPayments;
                stats.completedPayments = this.completedPayments;
                stats.failedPayments = this.failedPayments;
                stats.pendingPayments = this.pendingPayments;
                stats.totalRevenue = this.totalRevenue;
                stats.averageAmount = this.averageAmount;
                stats.providerStats = this.providerStats;
                stats.dailyRevenue = this.dailyRevenue;
                stats.monthlyRevenue = this.monthlyRevenue;
                return stats;
            }
        }

        // Getters and setters
        public Long getTotalPayments() { return totalPayments; }
        public void setTotalPayments(Long totalPayments) { this.totalPayments = totalPayments; }
        
        public Long getCompletedPayments() { return completedPayments; }
        public void setCompletedPayments(Long completedPayments) { this.completedPayments = completedPayments; }
        
        public Long getFailedPayments() { return failedPayments; }
        public void setFailedPayments(Long failedPayments) { this.failedPayments = failedPayments; }
        
        public Long getPendingPayments() { return pendingPayments; }
        public void setPendingPayments(Long pendingPayments) { this.pendingPayments = pendingPayments; }
        
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public BigDecimal getAverageAmount() { return averageAmount; }
        public void setAverageAmount(BigDecimal averageAmount) { this.averageAmount = averageAmount; }
        
        public List<Object[]> getProviderStats() { return providerStats; }
        public void setProviderStats(List<Object[]> providerStats) { this.providerStats = providerStats; }
        
        public List<Object[]> getDailyRevenue() { return dailyRevenue; }
        public void setDailyRevenue(List<Object[]> dailyRevenue) { this.dailyRevenue = dailyRevenue; }
        
        public List<Object[]> getMonthlyRevenue() { return monthlyRevenue; }
        public void setMonthlyRevenue(List<Object[]> monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
    }
}