package com.acanbiler.shopwave.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity representing payment transactions in the ShopWave application.
 * 
 * This entity provides provider abstraction to support multiple payment providers
 * and includes comprehensive payment tracking and status management.
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_user", columnList = "user_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_provider", columnList = "provider"),
    @Index(name = "idx_payment_provider_id", columnList = "provider_payment_id"),
    @Index(name = "idx_payment_created", columnList = "created_at"),
    @Index(name = "idx_payment_reference", columnList = "reference_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user"})
@ToString(exclude = {"user", "webhookData"})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code")
    @Column(nullable = false, length = 3)
    private String currency;

    @NotNull(message = "Payment provider is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentProvider provider;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Size(max = 100, message = "Provider payment ID must not exceed 100 characters")
    @Column(name = "provider_payment_id", length = 100)
    private String providerPaymentId;

    @NotBlank(message = "Reference number is required")
    @Size(max = 50, message = "Reference number must not exceed 50 characters")
    @Column(name = "reference_number", nullable = false, length = 50, unique = true)
    private String referenceNumber;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Size(max = 4, message = "Card last four digits must be 4 characters")
    @Pattern(regexp = "^\\d{4}$", message = "Card last four must be 4 digits")
    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Size(max = 50, message = "Card brand must not exceed 50 characters")
    @Column(name = "card_brand", length = 50)
    private String cardBrand;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Size(max = 500, message = "Failure reason must not exceed 500 characters")
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "webhook_data", columnDefinition = "TEXT")
    private String webhookData;

    @Column(name = "webhook_received_at")
    private LocalDateTime webhookReceivedAt;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "dispute_amount", precision = 12, scale = 2)
    private BigDecimal disputeAmount;

    @Column(name = "disputed_at")
    private LocalDateTime disputedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_user"))
    private User user;

    // Utility methods
    public boolean isSuccessful() {
        return PaymentStatus.COMPLETED.equals(status);
    }

    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(status);
    }

    public boolean isPending() {
        return PaymentStatus.PENDING.equals(status);
    }

    public boolean isRefunded() {
        return refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isPartiallyRefunded() {
        return refundAmount != null && 
               refundAmount.compareTo(BigDecimal.ZERO) > 0 && 
               refundAmount.compareTo(amount) < 0;
    }

    public boolean isFullyRefunded() {
        return refundAmount != null && refundAmount.compareTo(amount) >= 0;
    }

    public boolean isDisputed() {
        return disputeAmount != null && disputeAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getFormattedAmount() {
        return String.format("%.2f %s", amount, currency);
    }

    public BigDecimal getNetAmount() {
        BigDecimal net = amount;
        if (refundAmount != null) {
            net = net.subtract(refundAmount);
        }
        if (disputeAmount != null) {
            net = net.subtract(disputeAmount);
        }
        return net;
    }

    /**
     * Payment provider enumeration.
     */
    public enum PaymentProvider {
        IYZILINK("Iyzico Payment Provider"),
        STRIPE("Stripe Payment Provider"),
        PAYPAL("PayPal Payment Provider"),
        SQUARE("Square Payment Provider");

        private final String description;

        PaymentProvider(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Payment status enumeration.
     */
    public enum PaymentStatus {
        PENDING("Payment is pending processing"),
        PROCESSING("Payment is being processed"),
        COMPLETED("Payment completed successfully"),
        FAILED("Payment failed"),
        CANCELLED("Payment was cancelled"),
        REFUNDED("Payment was refunded"),
        PARTIALLY_REFUNDED("Payment was partially refunded"),
        DISPUTED("Payment is disputed");

        private final String description;

        PaymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isFinal() {
            return this == COMPLETED || this == FAILED || this == CANCELLED;
        }

        public boolean canBeRefunded() {
            return this == COMPLETED || this == PARTIALLY_REFUNDED;
        }
    }

    /**
     * Payment method enumeration.
     */
    public enum PaymentMethod {
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        BANK_TRANSFER("Bank Transfer"),
        DIGITAL_WALLET("Digital Wallet"),
        CRYPTOCURRENCY("Cryptocurrency");

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}