package com.shopwave.service;

import com.shopwave.dto.OrderDto;
import com.shopwave.model.Order;

import java.util.List;

public interface OrderService extends BaseService<Order, OrderDto> {
    List<OrderDto> findOrdersByUserId(Long userId);
    void processPayment(Long orderId, String paymentToken);
    void cancelOrder(Long orderId);
    void updateOrderStatus(Long orderId, Order.OrderStatus status);
    List<OrderDto> findByStatus(Order.OrderStatus status);
    List<OrderDto> findOrdersByDateRange(String startDate, String endDate);
} 