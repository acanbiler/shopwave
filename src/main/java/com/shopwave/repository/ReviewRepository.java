package com.shopwave.repository;

import com.shopwave.model.Product;
import com.shopwave.model.Review;
import com.shopwave.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
    List<Review> findByUser(User user);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product")
    Double getAverageRating(@Param("product") Product product);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product = :product")
    Long getReviewCount(@Param("product") Product product);
} 