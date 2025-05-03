package com.shopwave.service.impl;

import com.iyzipay.model.*;
import com.iyzipay.request.*;
import com.shopwave.config.IyzicoConfig;
import com.shopwave.dto.OrderDto;
import com.shopwave.exception.PaymentException;
import com.shopwave.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final IyzicoConfig iyzicoConfig;

    @Override
    public Payment initializePayment(OrderDto orderDto) {
        try {
            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setLocale(Locale.TR.getValue());
            request.setConversationId(orderDto.getId().toString());
            request.setPrice(BigDecimal.valueOf(orderDto.getTotalAmount()));
            request.setPaidPrice(BigDecimal.valueOf(orderDto.getTotalAmount()));
            request.setCurrency(Currency.TRY.name());
            request.setInstallment(1);
            request.setBasketId(orderDto.getId().toString());
            request.setPaymentChannel(PaymentChannel.WEB.name());
            request.setPaymentGroup(PaymentGroup.PRODUCT.name());

            PaymentCard paymentCard = new PaymentCard();
            // Payment card details will be provided by the client
            request.setPaymentCard(paymentCard);

            Buyer buyer = new Buyer();
            buyer.setId(orderDto.getUserId().toString());
            buyer.setName(orderDto.getUser().getFirstName());
            buyer.setSurname(orderDto.getUser().getLastName());
            buyer.setEmail(orderDto.getUser().getEmail());
            buyer.setGsmNumber(orderDto.getUser().getPhoneNumber());
            buyer.setIdentityNumber(orderDto.getUser().getIdentityNumber());
            buyer.setRegistrationAddress(orderDto.getBillingAddress().getStreet());
            buyer.setCity(orderDto.getBillingAddress().getCity());
            buyer.setCountry(orderDto.getBillingAddress().getCountry());
            buyer.setZipCode(orderDto.getBillingAddress().getZipCode());
            request.setBuyer(buyer);

            Address shippingAddress = new Address();
            shippingAddress.setContactName(orderDto.getShippingAddress().getFirstName() + " " + orderDto.getShippingAddress().getLastName());
            shippingAddress.setCity(orderDto.getShippingAddress().getCity());
            shippingAddress.setCountry(orderDto.getShippingAddress().getCountry());
            shippingAddress.setAddress(orderDto.getShippingAddress().getStreet());
            shippingAddress.setZipCode(orderDto.getShippingAddress().getZipCode());
            request.setShippingAddress(shippingAddress);

            Address billingAddress = new Address();
            billingAddress.setContactName(orderDto.getBillingAddress().getFirstName() + " " + orderDto.getBillingAddress().getLastName());
            billingAddress.setCity(orderDto.getBillingAddress().getCity());
            billingAddress.setCountry(orderDto.getBillingAddress().getCountry());
            billingAddress.setAddress(orderDto.getBillingAddress().getStreet());
            billingAddress.setZipCode(orderDto.getBillingAddress().getZipCode());
            request.setBillingAddress(billingAddress);

            List<BasketItem> basketItems = new ArrayList<>();
            orderDto.getOrderItems().forEach(item -> {
                BasketItem basketItem = new BasketItem();
                basketItem.setId(item.getProductId().toString());
                basketItem.setName(item.getProductName());
                basketItem.setCategory1(item.getProduct().getCategory().getName());
                basketItem.setItemType(BasketItemType.PHYSICAL.name());
                basketItem.setPrice(BigDecimal.valueOf(item.getPrice()));
                basketItems.add(basketItem);
            });
            request.setBasketItems(basketItems);

            return Payment.create(request, iyzicoConfig.getOptions());
        } catch (Exception e) {
            log.error("Error initializing payment: {}", e.getMessage());
            throw new PaymentException("Failed to initialize payment", e);
        }
    }

    @Override
    public Payment completePayment(String paymentToken, OrderDto orderDto) {
        try {
            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setLocale(Locale.TR.getValue());
            request.setConversationId(orderDto.getId().toString());
            request.setPaymentId(paymentToken);

            Payment payment = Payment.create(request, iyzicoConfig.getOptions());
            if (!"success".equals(payment.getStatus())) {
                throw new PaymentException("Payment failed: " + payment.getErrorMessage());
            }
            return payment;
        } catch (Exception e) {
            log.error("Error completing payment: {}", e.getMessage());
            throw new PaymentException("Failed to complete payment", e);
        }
    }

    @Override
    public void refundPayment(Order order, double amount) {
        try {
            CreateRefundRequest request = new CreateRefundRequest();
            request.setLocale(Locale.TR.getValue());
            request.setConversationId(order.getId().toString());
            request.setPaymentTransactionId(order.getPaymentTransactionId());
            request.setPrice(BigDecimal.valueOf(amount));
            request.setCurrency(Currency.TRY.name());
            request.setIp("127.0.0.1");

            Refund refund = Refund.create(request, iyzicoConfig.getOptions());
            if (!"success".equals(refund.getStatus())) {
                throw new PaymentException("Refund failed: " + refund.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage());
            throw new PaymentException("Failed to process refund", e);
        }
    }

    @Override
    public Payment retrievePayment(String paymentId) {
        try {
            RetrievePaymentRequest request = new RetrievePaymentRequest();
            request.setLocale(Locale.TR.getValue());
            request.setPaymentId(paymentId);

            return Payment.retrieve(request, iyzicoConfig.getOptions());
        } catch (Exception e) {
            log.error("Error retrieving payment: {}", e.getMessage());
            throw new PaymentException("Failed to retrieve payment", e);
        }
    }

    @Override
    public void cancelPayment(String paymentId) {
        try {
            CreateCancelRequest request = new CreateCancelRequest();
            request.setLocale(Locale.TR.getValue());
            request.setPaymentId(paymentId);

            Cancel cancel = Cancel.create(request, iyzicoConfig.getOptions());
            if (!"success".equals(cancel.getStatus())) {
                throw new PaymentException("Cancel failed: " + cancel.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Error canceling payment: {}", e.getMessage());
            throw new PaymentException("Failed to cancel payment", e);
        }
    }
} 