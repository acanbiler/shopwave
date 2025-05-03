package com.shopwave.repository;

import com.shopwave.model.Order;
import com.shopwave.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status = :status")
    List<Order> findByUserAndStatus(@Param("user") User user, @Param("status") Order.OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") Order.OrderStatus status);
} 