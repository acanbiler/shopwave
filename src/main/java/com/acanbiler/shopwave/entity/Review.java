package com.acanbiler.shopwave.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Review entity representing product reviews in the ShopWave application.
 * 
 * This entity includes review information with rating validation
 * and relationships to both users and products.
 */
@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_review_product", columnList = "product_id"),
    @Index(name = "idx_review_user", columnList = "user_id"),
    @Index(name = "idx_review_rating", columnList = "rating"),
    @Index(name = "idx_review_created", columnList = "created_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_review_user_product", columnNames = {"user_id", "product_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "product"})
@ToString(exclude = {"user", "product"})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(nullable = false)
    private Integer rating;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    @Column(length = 1000)
    private String comment;

    @Size(max = 100, message = "Title must not exceed 100 characters")
    @Column(length = 100)
    private String title;

    @Column(name = "verified_purchase")
    @Builder.Default
    private Boolean verifiedPurchase = false;

    @Column(name = "helpful_count")
    @Builder.Default
    private Integer helpfulCount = 0;

    @Column(name = "reported_count")
    @Builder.Default
    private Integer reportedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.APPROVED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_product"))
    private Product product;

    // Utility methods
    public boolean isPositiveReview() {
        return rating != null && rating >= 4;
    }

    public boolean isNegativeReview() {
        return rating != null && rating <= 2;
    }

    public boolean isNeutralReview() {
        return rating != null && rating == 3;
    }

    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }

    public boolean hasTitle() {
        return title != null && !title.trim().isEmpty();
    }

    public void incrementHelpfulCount() {
        this.helpfulCount = (this.helpfulCount == null ? 0 : this.helpfulCount) + 1;
    }

    public void decrementHelpfulCount() {
        if (this.helpfulCount != null && this.helpfulCount > 0) {
            this.helpfulCount--;
        }
    }

    public void incrementReportedCount() {
        this.reportedCount = (this.reportedCount == null ? 0 : this.reportedCount) + 1;
    }

    public boolean isReported() {
        return reportedCount != null && reportedCount > 0;
    }

    public boolean isHelpful() {
        return helpfulCount != null && helpfulCount > 0;
    }

    public String getRatingStars() {
        if (rating == null) return "";
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    public String getShortComment() {
        if (comment == null || comment.length() <= 100) {
            return comment;
        }
        return comment.substring(0, 97) + "...";
    }

    /**
     * Review status enumeration.
     */
    public enum ReviewStatus {
        PENDING("Review is pending moderation"),
        APPROVED("Review is approved and visible"),
        REJECTED("Review is rejected and hidden"),
        FLAGGED("Review is flagged for review");

        private final String description;

        ReviewStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isVisible() {
            return this == APPROVED;
        }

        public boolean needsModeration() {
            return this == PENDING || this == FLAGGED;
        }
    }
}