package com.shopwave.controller;

import com.shopwave.dto.ReviewDto;
import com.shopwave.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@RequestBody ReviewDto reviewDto) {
        return ResponseEntity.ok(reviewService.create(reviewDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long id, @RequestBody ReviewDto reviewDto) {
        return ResponseEntity.ok(reviewService.update(id, reviewDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.findByProductId(productId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.findByUserId(userId));
    }

    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<ReviewDto>> getReviewsByRating(@PathVariable Integer rating) {
        return ResponseEntity.ok(reviewService.findByRating(rating));
    }

    @GetMapping("/product/{productId}/average-rating")
    public ResponseEntity<Double> getAverageRatingForProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getAverageRatingForProduct(productId));
    }

    @GetMapping("/product/{productId}/count")
    public ResponseEntity<Integer> getReviewCountForProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewCountForProduct(productId));
    }

    @GetMapping("/check-review")
    public ResponseEntity<Boolean> checkUserReview(
            @RequestParam Long userId,
            @RequestParam Long productId) {
        return ResponseEntity.ok(reviewService.hasUserReviewedProduct(userId, productId));
    }
} 