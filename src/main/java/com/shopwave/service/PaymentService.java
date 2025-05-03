package com.shopwave.service;

import com.iyzipay.model.Payment;
import com.shopwave.dto.OrderDto;
import com.shopwave.model.Order;

public interface PaymentService {
    Payment initializePayment(OrderDto orderDto);
    Payment completePayment(String paymentToken, OrderDto orderDto);
    void refundPayment(Order order, double amount);
    Payment retrievePayment(String paymentId);
    void cancelPayment(String paymentId);
} 