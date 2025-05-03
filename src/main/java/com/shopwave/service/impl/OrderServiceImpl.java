package com.shopwave.service.impl;

import com.shopwave.dto.OrderDto;
import com.shopwave.exception.ResourceNotFoundException;
import com.shopwave.mapper.OrderMapper;
import com.shopwave.model.Order;
import com.shopwave.model.OrderItem;
import com.shopwave.model.Product;
import com.shopwave.repository.OrderRepository;
import com.shopwave.repository.ProductRepository;
import com.shopwave.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderDto create(OrderDto orderDto) {
        Order order = orderMapper.toEntity(orderDto);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDto update(Long id, OrderDto orderDto) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        Order updatedOrder = orderMapper.toEntity(orderDto);
        updatedOrder.setId(existingOrder.getId());
        updatedOrder.setCreatedAt(existingOrder.getCreatedAt());
        updatedOrder.setStatus(existingOrder.getStatus());
        updatedOrder.setUser(existingOrder.getUser());

        Order savedOrder = orderRepository.save(updatedOrder);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be cancelled");
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void processPayment(Long orderId, String paymentToken) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        order.setPaymentToken(paymentToken);
        order.setStatus(Order.OrderStatus.PAID);
        order.setPaymentDate(LocalDateTime.now());
        
        orderRepository.save(order);
        
        // Update product stock quantities
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findOrdersByDateRange(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        
        return orderRepository.findByCreatedAtBetween(start, end).stream()
                .map(orderMapper::toDto)
                .toList();
    }
} 