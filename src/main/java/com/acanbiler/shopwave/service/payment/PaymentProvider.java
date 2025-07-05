package com.acanbiler.shopwave.service.payment;

import com.acanbiler.shopwave.entity.Payment;

/**
 * Payment provider interface for processing payments through different providers.
 * 
 * This interface provides abstraction for payment processing operations
 * including payment initiation, webhook processing, and refunds.
 */
public interface PaymentProvider {

    /**
     * Process a payment transaction.
     * 
     * @param request payment processing request
     * @return payment processing response
     * @throws PaymentProcessingException if payment processing fails
     */
    PaymentResponse processPayment(PaymentRequest request) throws PaymentProcessingException;

    /**
     * Process webhook notification from payment provider.
     * 
     * @param webhookData raw webhook data
     * @param signature webhook signature for verification
     * @return webhook processing response
     * @throws PaymentProcessingException if webhook processing fails
     */
    WebhookResponse processWebhook(String webhookData, String signature) throws PaymentProcessingException;

    /**
     * Process refund for a payment.
     * 
     * @param request refund processing request
     * @return refund processing response
     * @throws PaymentProcessingException if refund processing fails
     */
    RefundResponse processRefund(RefundRequest request) throws PaymentProcessingException;

    /**
     * Get provider name.
     * 
     * @return provider name
     */
    String getProviderName();

    /**
     * Check if provider supports specific payment method.
     * 
     * @param paymentMethod payment method to check
     * @return true if supported, false otherwise
     */
    boolean supportsPaymentMethod(Payment.PaymentMethod paymentMethod);

    /**
     * Validate webhook signature.
     * 
     * @param webhookData raw webhook data
     * @param signature webhook signature
     * @return true if valid, false otherwise
     */
    boolean validateWebhookSignature(String webhookData, String signature);

    /**
     * Payment request DTO.
     */
    class PaymentRequest {
        private String amount;
        private String currency;
        private String paymentMethod;
        private String cardNumber;
        private String expiryMonth;
        private String expiryYear;
        private String cvc;
        private String cardHolderName;
        private String description;
        private String referenceNumber;
        private CustomerInfo customer;
        private BillingAddress billingAddress;

        // Constructors
        public PaymentRequest() {}

        // Getters and setters
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

        public String getExpiryMonth() { return expiryMonth; }
        public void setExpiryMonth(String expiryMonth) { this.expiryMonth = expiryMonth; }

        public String getExpiryYear() { return expiryYear; }
        public void setExpiryYear(String expiryYear) { this.expiryYear = expiryYear; }

        public String getCvc() { return cvc; }
        public void setCvc(String cvc) { this.cvc = cvc; }

        public String getCardHolderName() { return cardHolderName; }
        public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

        public CustomerInfo getCustomer() { return customer; }
        public void setCustomer(CustomerInfo customer) { this.customer = customer; }

        public BillingAddress getBillingAddress() { return billingAddress; }
        public void setBillingAddress(BillingAddress billingAddress) { this.billingAddress = billingAddress; }
    }

    /**
     * Payment response DTO.
     */
    class PaymentResponse {
        private boolean success;
        private String providerPaymentId;
        private String status;
        private String errorMessage;
        private String errorCode;
        private String authCode;
        private String hostReference;
        private String cardLastFour;
        private String cardBrand;

        // Constructors
        public PaymentResponse() {}

        public PaymentResponse(boolean success, String providerPaymentId, String status) {
            this.success = success;
            this.providerPaymentId = providerPaymentId;
            this.status = status;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getProviderPaymentId() { return providerPaymentId; }
        public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

        public String getAuthCode() { return authCode; }
        public void setAuthCode(String authCode) { this.authCode = authCode; }

        public String getHostReference() { return hostReference; }
        public void setHostReference(String hostReference) { this.hostReference = hostReference; }

        public String getCardLastFour() { return cardLastFour; }
        public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }

        public String getCardBrand() { return cardBrand; }
        public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
    }

    /**
     * Webhook response DTO.
     */
    class WebhookResponse {
        private boolean success;
        private String paymentId;
        private String status;
        private String errorMessage;

        // Constructors
        public WebhookResponse() {}

        public WebhookResponse(boolean success, String paymentId, String status) {
            this.success = success;
            this.paymentId = paymentId;
            this.status = status;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * Refund request DTO.
     */
    class RefundRequest {
        private String providerPaymentId;
        private String amount;
        private String currency;
        private String reason;
        private String referenceNumber;

        // Constructors
        public RefundRequest() {}

        public RefundRequest(String providerPaymentId, String amount, String currency, String reason) {
            this.providerPaymentId = providerPaymentId;
            this.amount = amount;
            this.currency = currency;
            this.reason = reason;
        }

        // Getters and setters
        public String getProviderPaymentId() { return providerPaymentId; }
        public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }

        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    }

    /**
     * Refund response DTO.
     */
    class RefundResponse {
        private boolean success;
        private String refundId;
        private String status;
        private String errorMessage;
        private String errorCode;

        // Constructors
        public RefundResponse() {}

        public RefundResponse(boolean success, String refundId, String status) {
            this.success = success;
            this.refundId = refundId;
            this.status = status;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getRefundId() { return refundId; }
        public void setRefundId(String refundId) { this.refundId = refundId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    }

    /**
     * Customer information DTO.
     */
    class CustomerInfo {
        private String id;
        private String name;
        private String surname;
        private String email;
        private String phone;
        private String identityNumber;

        // Constructors
        public CustomerInfo() {}

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSurname() { return surname; }
        public void setSurname(String surname) { this.surname = surname; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getIdentityNumber() { return identityNumber; }
        public void setIdentityNumber(String identityNumber) { this.identityNumber = identityNumber; }
    }

    /**
     * Billing address DTO.
     */
    class BillingAddress {
        private String contactName;
        private String city;
        private String country;
        private String address;
        private String zipCode;

        // Constructors
        public BillingAddress() {}

        // Getters and setters
        public String getContactName() { return contactName; }
        public void setContactName(String contactName) { this.contactName = contactName; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    }
}