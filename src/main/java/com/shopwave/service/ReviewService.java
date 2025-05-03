package com.shopwave.service;

import com.shopwave.dto.ReviewDto;
import com.shopwave.model.Review;

import java.util.List;

public interface ReviewService extends BaseService<Review, ReviewDto> {
    List<ReviewDto> findByProductId(Long productId);
    List<ReviewDto> findByUserId(Long userId);
    List<ReviewDto> findByRating(Integer rating);
    double getAverageRatingForProduct(Long productId);
    int getReviewCountForProduct(Long productId);
    boolean hasUserReviewedProduct(Long userId, Long productId);
} 