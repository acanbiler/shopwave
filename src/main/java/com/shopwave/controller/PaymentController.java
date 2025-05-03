package com.shopwave.controller;

import com.iyzipay.model.Payment;
import com.shopwave.dto.OrderDto;
import com.shopwave.service.OrderService;
import com.shopwave.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping("/initialize")
    public ResponseEntity<Payment> initializePayment(@RequestBody OrderDto orderDto) {
        return ResponseEntity.ok(paymentService.initializePayment(orderDto));
    }

    @PostMapping("/complete")
    public ResponseEntity<Payment> completePayment(
            @RequestParam String paymentToken,
            @RequestBody OrderDto orderDto) {
        Payment payment = paymentService.completePayment(paymentToken, orderDto);
        if (payment.getStatus().equals("success")) {
            orderService.processPayment(orderDto.getId(), paymentToken);
        }
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<Void> refundPayment(
            @PathVariable Long orderId,
            @RequestParam double amount) {
        OrderDto order = orderService.getById(orderId);
        paymentService.refundPayment(order, amount);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.retrievePayment(paymentId));
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Void> cancelPayment(@PathVariable String paymentId) {
        paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok().build();
    }
} 