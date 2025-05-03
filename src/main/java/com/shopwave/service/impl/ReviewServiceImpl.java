package com.shopwave.service.impl;

import com.shopwave.dto.ReviewDto;
import com.shopwave.exception.ResourceNotFoundException;
import com.shopwave.mapper.ReviewMapper;
import com.shopwave.model.Review;
import com.shopwave.repository.ReviewRepository;
import com.shopwave.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewDto create(ReviewDto reviewDto) {
        Review review = reviewMapper.toEntity(reviewDto);
        review.setCreatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getById(Long id) {
        return reviewRepository.findById(id)
                .map(reviewMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getAll() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ReviewDto update(Long id, ReviewDto reviewDto) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        Review updatedReview = reviewMapper.toEntity(reviewDto);
        updatedReview.setId(existingReview.getId());
        updatedReview.setCreatedAt(existingReview.getCreatedAt());
        updatedReview.setUser(existingReview.getUser());
        updatedReview.setProduct(existingReview.getProduct());

        Review savedReview = reviewRepository.save(updatedReview);
        return reviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> findByProductId(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> findByUserId(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> findByRating(Integer rating) {
        return reviewRepository.findByRating(rating).stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageRatingForProduct(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public int getReviewCountForProduct(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedProduct(Long userId, Long productId) {
        return reviewRepository.existsByUserIdAndProductId(userId, productId);
    }
} 