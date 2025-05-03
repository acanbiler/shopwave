package com.shopwave.service;

import com.iyzipay.model.Payment;
import com.iyzipay.model.Status;
import com.shopwave.config.IyzicoConfig;
import com.shopwave.dto.OrderDto;
import com.shopwave.exception.PaymentException;
import com.shopwave.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private IyzicoConfig iyzicoConfig;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private OrderDto orderDto;
    private Payment payment;

    @BeforeEach
    void setUp() {
        orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setTotalAmount(100.0);

        payment = new Payment();
        payment.setStatus(Status.SUCCESS.getValue());
        payment.setPaymentId("test_payment_id");
        payment.setPrice(BigDecimal.valueOf(100.0));
    }

    @Test
    void initializePayment_ShouldReturnPayment() {
        when(iyzicoConfig.getOptions()).thenReturn(any());
        when(Payment.create(any(), any())).thenReturn(payment);

        Payment result = paymentService.initializePayment(orderDto);

        assertNotNull(result);
        assertEquals(payment.getStatus(), result.getStatus());
        assertEquals(payment.getPaymentId(), result.getPaymentId());
        assertEquals(payment.getPrice(), result.getPrice());
    }

    @Test
    void completePayment_ShouldReturnPayment() {
        when(iyzicoConfig.getOptions()).thenReturn(any());
        when(Payment.create(any(), any())).thenReturn(payment);

        Payment result = paymentService.completePayment("test_token", orderDto);

        assertNotNull(result);
        assertEquals(payment.getStatus(), result.getStatus());
        assertEquals(payment.getPaymentId(), result.getPaymentId());
        assertEquals(payment.getPrice(), result.getPrice());
    }

    @Test
    void completePayment_WhenPaymentFails_ShouldThrowException() {
        Payment failedPayment = new Payment();
        failedPayment.setStatus(Status.FAILURE.getValue());
        failedPayment.setErrorMessage("Payment failed");

        when(iyzicoConfig.getOptions()).thenReturn(any());
        when(Payment.create(any(), any())).thenReturn(failedPayment);

        assertThrows(PaymentException.class, () -> 
            paymentService.completePayment("test_token", orderDto));
    }

    @Test
    void retrievePayment_ShouldReturnPayment() {
        when(iyzicoConfig.getOptions()).thenReturn(any());
        when(Payment.retrieve(any(), any())).thenReturn(payment);

        Payment result = paymentService.retrievePayment("test_payment_id");

        assertNotNull(result);
        assertEquals(payment.getStatus(), result.getStatus());
        assertEquals(payment.getPaymentId(), result.getPaymentId());
        assertEquals(payment.getPrice(), result.getPrice());
    }

    @Test
    void cancelPayment_ShouldSucceed() {
        Payment cancelledPayment = new Payment();
        cancelledPayment.setStatus(Status.SUCCESS.getValue());

        when(iyzicoConfig.getOptions()).thenReturn(any());
        when(com.iyzipay.model.Cancel.create(any(), any())).thenReturn(cancelledPayment);

        assertDoesNotThrow(() -> paymentService.cancelPayment("test_payment_id"));
    }

    @Test
    void cancelPayment_WhenCancellationFails_ShouldThrowException() {
        Payment failedCancellation = new Payment();
        failedCancellation.setStatus(Status.FAILURE.getValue());
        failedCancellation.setErrorMessage("Cancellation failed");

        when(iyzicoConfig.getOptions()).thenReturn(any());
        when(com.iyzipay.model.Cancel.create(any(), any())).thenReturn(failedCancellation);

        assertThrows(PaymentException.class, () -> 
            paymentService.cancelPayment("test_payment_id"));
    }
} 