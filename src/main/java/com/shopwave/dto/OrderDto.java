package com.shopwave.dto;

import com.shopwave.model.Order;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderDto extends BaseDto {
    private Long userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    private String paymentStatus;
    private String trackingNumber;
    private List<OrderItemDto> orderItems;
} 