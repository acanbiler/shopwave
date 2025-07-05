package com.acanbiler.shopwave.service.payment;

import com.acanbiler.shopwave.config.PaymentConfig;
import com.acanbiler.shopwave.entity.Payment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Webhook verification service for payment providers.
 * 
 * This service handles webhook signature verification and provides
 * secure webhook processing capabilities for different payment providers.
 */
@Service
public class WebhookVerificationService {

    private final PaymentConfig paymentConfig;
    private final ObjectMapper objectMapper;

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int WEBHOOK_TIMEOUT_MINUTES = 5;

    public WebhookVerificationService(PaymentConfig paymentConfig) {
        this.paymentConfig = paymentConfig;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Verify webhook signature for specific payment provider.
     * 
     * @param provider payment provider
     * @param webhookData raw webhook data
     * @param signature webhook signature
     * @return true if signature is valid, false otherwise
     */
    public boolean verifyWebhookSignature(Payment.PaymentProvider provider, String webhookData, String signature) {
        try {
            PaymentConfig.ProviderConfig config = getProviderConfig(provider);
            if (config == null || config.getWebhookSecret() == null) {
                return false;
            }

            String expectedSignature = generateHmacSignature(webhookData, config.getWebhookSecret());
            return constantTimeEquals(expectedSignature, signature);
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse webhook data and extract payment information.
     * 
     * @param provider payment provider
     * @param webhookData raw webhook data
     * @return webhook payment data
     * @throws PaymentProcessingException if parsing fails
     */
    public WebhookPaymentData parseWebhookData(Payment.PaymentProvider provider, String webhookData) 
            throws PaymentProcessingException {
        try {
            JsonNode webhookJson = objectMapper.readTree(webhookData);
            
            return switch (provider) {
                case IYZILINK -> parseIyziLinkWebhook(webhookJson);
                case STRIPE -> parseStripeWebhook(webhookJson);
                case PAYPAL -> parsePayPalWebhook(webhookJson);
                case SQUARE -> parseSquareWebhook(webhookJson);
            };
            
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to parse webhook data", e);
        }
    }

    /**
     * Validate webhook timestamp to prevent replay attacks.
     * 
     * @param timestamp webhook timestamp
     * @return true if timestamp is valid, false otherwise
     */
    public boolean isWebhookTimestampValid(LocalDateTime timestamp) {
        if (timestamp == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earliestValid = now.minusMinutes(WEBHOOK_TIMEOUT_MINUTES);
        LocalDateTime latestValid = now.plusMinutes(1); // Allow 1 minute clock skew
        
        return timestamp.isAfter(earliestValid) && timestamp.isBefore(latestValid);
    }

    /**
     * Generate HMAC signature for webhook data.
     * 
     * @param data data to sign
     * @param secret webhook secret
     * @return base64 encoded signature
     * @throws NoSuchAlgorithmException if HMAC algorithm is not available
     * @throws InvalidKeyException if secret key is invalid
     */
    private String generateHmacSignature(String data, String secret) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Constant time string comparison to prevent timing attacks.
     * 
     * @param a first string
     * @param b second string
     * @return true if strings are equal, false otherwise
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }

    /**
     * Get provider configuration for specific payment provider.
     * 
     * @param provider payment provider
     * @return provider configuration or null if not found
     */
    private PaymentConfig.ProviderConfig getProviderConfig(Payment.PaymentProvider provider) {
        String providerName = provider.name().toLowerCase();
        return paymentConfig.getProviders().get(providerName);
    }

    /**
     * Parse IyziLink webhook data.
     * 
     * @param webhookJson webhook JSON data
     * @return webhook payment data
     */
    private WebhookPaymentData parseIyziLinkWebhook(JsonNode webhookJson) {
        WebhookPaymentData data = new WebhookPaymentData();
        data.setProviderPaymentId(webhookJson.path("paymentId").asText());
        data.setStatus(webhookJson.path("status").asText());
        data.setAmount(webhookJson.path("price").asText());
        data.setCurrency(webhookJson.path("currency").asText());
        data.setReferenceNumber(webhookJson.path("basketId").asText());
        return data;
    }

    /**
     * Parse Stripe webhook data.
     * 
     * @param webhookJson webhook JSON data
     * @return webhook payment data
     */
    private WebhookPaymentData parseStripeWebhook(JsonNode webhookJson) {
        WebhookPaymentData data = new WebhookPaymentData();
        JsonNode paymentIntent = webhookJson.path("data").path("object");
        data.setProviderPaymentId(paymentIntent.path("id").asText());
        data.setStatus(paymentIntent.path("status").asText());
        data.setAmount(String.valueOf(paymentIntent.path("amount").asLong() / 100.0));
        data.setCurrency(paymentIntent.path("currency").asText().toUpperCase());
        data.setReferenceNumber(paymentIntent.path("metadata").path("reference").asText());
        return data;
    }

    /**
     * Parse PayPal webhook data.
     * 
     * @param webhookJson webhook JSON data
     * @return webhook payment data
     */
    private WebhookPaymentData parsePayPalWebhook(JsonNode webhookJson) {
        WebhookPaymentData data = new WebhookPaymentData();
        JsonNode resource = webhookJson.path("resource");
        data.setProviderPaymentId(resource.path("id").asText());
        data.setStatus(resource.path("state").asText());
        JsonNode amount = resource.path("amount");
        data.setAmount(amount.path("total").asText());
        data.setCurrency(amount.path("currency").asText());
        data.setReferenceNumber(resource.path("custom").asText());
        return data;
    }

    /**
     * Parse Square webhook data.
     * 
     * @param webhookJson webhook JSON data
     * @return webhook payment data
     */
    private WebhookPaymentData parseSquareWebhook(JsonNode webhookJson) {
        WebhookPaymentData data = new WebhookPaymentData();
        JsonNode payment = webhookJson.path("data").path("object").path("payment");
        data.setProviderPaymentId(payment.path("id").asText());
        data.setStatus(payment.path("status").asText());
        data.setAmount(String.valueOf(payment.path("amount_money").path("amount").asLong() / 100.0));
        data.setCurrency(payment.path("amount_money").path("currency").asText());
        data.setReferenceNumber(payment.path("reference_id").asText());
        return data;
    }

    /**
     * Webhook payment data DTO.
     */
    public static class WebhookPaymentData {
        private String providerPaymentId;
        private String status;
        private String amount;
        private String currency;
        private String referenceNumber;
        private String errorMessage;
        private String errorCode;

        // Constructors
        public WebhookPaymentData() {}

        // Getters and setters
        public String getProviderPaymentId() { return providerPaymentId; }
        public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    }
}