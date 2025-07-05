package com.acanbiler.shopwave.service.payment;

import com.acanbiler.shopwave.entity.Payment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * IyziLink payment provider implementation.
 * 
 * This implementation provides integration with IyziLink payment system
 * including payment processing, webhook handling, and refund operations.
 */
public class IyziLinkPaymentProvider implements PaymentProvider {

    private final String apiKey;
    private final String secretKey;
    private final String apiUrl;
    private final boolean testMode;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Constructor for IyziLink payment provider.
     * 
     * @param apiKey IyziLink API key
     * @param secretKey IyziLink secret key
     * @param apiUrl IyziLink API URL
     * @param testMode whether to use test mode
     * @param restTemplate REST template for HTTP communication
     */
    public IyziLinkPaymentProvider(String apiKey, String secretKey, String apiUrl, boolean testMode, RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.apiUrl = apiUrl;
        this.testMode = testMode;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) throws PaymentProcessingException {
        try {
            // Create payment request payload
            Map<String, Object> payload = createPaymentPayload(request);
            
            // Create HTTP headers with authentication
            HttpHeaders headers = createAuthHeaders("POST", "/payment/auth", payload);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            
            // Send payment request
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl + "/payment/auth",
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // Parse response
            return parsePaymentResponse(response.getBody());
            
        } catch (Exception e) {
            throw new PaymentProcessingException("Payment processing failed", e);
        }
    }

    @Override
    public WebhookResponse processWebhook(String webhookData, String signature) throws PaymentProcessingException {
        try {
            // Validate webhook signature
            if (!validateWebhookSignature(webhookData, signature)) {
                throw new PaymentProcessingException("Invalid webhook signature");
            }
            
            // Parse webhook data
            JsonNode webhookJson = objectMapper.readTree(webhookData);
            String status = webhookJson.path("status").asText();
            String paymentId = webhookJson.path("paymentId").asText();
            
            return new WebhookResponse(true, paymentId, status);
            
        } catch (Exception e) {
            throw new PaymentProcessingException("Webhook processing failed", e);
        }
    }

    @Override
    public RefundResponse processRefund(RefundRequest request) throws PaymentProcessingException {
        try {
            // Create refund request payload
            Map<String, Object> payload = createRefundPayload(request);
            
            // Create HTTP headers with authentication
            HttpHeaders headers = createAuthHeaders("POST", "/payment/refund", payload);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            
            // Send refund request
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl + "/payment/refund",
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // Parse response
            return parseRefundResponse(response.getBody());
            
        } catch (Exception e) {
            throw new PaymentProcessingException("Refund processing failed", e);
        }
    }

    @Override
    public String getProviderName() {
        return "IyziLink";
    }

    @Override
    public boolean supportsPaymentMethod(Payment.PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case CREDIT_CARD, DEBIT_CARD -> true;
            case BANK_TRANSFER, DIGITAL_WALLET, CRYPTOCURRENCY -> false;
        };
    }

    @Override
    public boolean validateWebhookSignature(String webhookData, String signature) {
        try {
            String expectedSignature = generateSignature(webhookData);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Create payment payload for IyziLink API.
     * 
     * @param request payment request
     * @return payment payload
     */
    private Map<String, Object> createPaymentPayload(PaymentRequest request) {
        Map<String, Object> payload = new HashMap<>();
        
        // Basic payment information
        payload.put("price", request.getAmount());
        payload.put("paidPrice", request.getAmount());
        payload.put("currency", request.getCurrency());
        payload.put("installment", "1");
        payload.put("basketId", request.getReferenceNumber());
        payload.put("paymentChannel", "WEB");
        payload.put("paymentGroup", "PRODUCT");
        
        // Payment card information
        Map<String, Object> paymentCard = new HashMap<>();
        paymentCard.put("cardHolderName", request.getCardHolderName());
        paymentCard.put("cardNumber", request.getCardNumber());
        paymentCard.put("expireMonth", request.getExpiryMonth());
        paymentCard.put("expireYear", request.getExpiryYear());
        paymentCard.put("cvc", request.getCvc());
        payload.put("paymentCard", paymentCard);
        
        // Customer information
        if (request.getCustomer() != null) {
            Map<String, Object> buyer = new HashMap<>();
            buyer.put("id", request.getCustomer().getId());
            buyer.put("name", request.getCustomer().getName());
            buyer.put("surname", request.getCustomer().getSurname());
            buyer.put("email", request.getCustomer().getEmail());
            buyer.put("identityNumber", request.getCustomer().getIdentityNumber());
            buyer.put("registrationAddress", "Test Address");
            buyer.put("city", "Istanbul");
            buyer.put("country", "Turkey");
            buyer.put("zipCode", "34732");
            payload.put("buyer", buyer);
        }
        
        // Billing address
        if (request.getBillingAddress() != null) {
            Map<String, Object> billingAddress = new HashMap<>();
            billingAddress.put("contactName", request.getBillingAddress().getContactName());
            billingAddress.put("city", request.getBillingAddress().getCity());
            billingAddress.put("country", request.getBillingAddress().getCountry());
            billingAddress.put("address", request.getBillingAddress().getAddress());
            billingAddress.put("zipCode", request.getBillingAddress().getZipCode());
            payload.put("billingAddress", billingAddress);
        }
        
        return payload;
    }

    /**
     * Create refund payload for IyziLink API.
     * 
     * @param request refund request
     * @return refund payload
     */
    private Map<String, Object> createRefundPayload(RefundRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentTransactionId", request.getProviderPaymentId());
        payload.put("price", request.getAmount());
        payload.put("currency", request.getCurrency());
        payload.put("ip", "127.0.0.1");
        return payload;
    }

    /**
     * Create authentication headers for IyziLink API.
     * 
     * @param method HTTP method
     * @param uri request URI
     * @param payload request payload
     * @return HTTP headers
     * @throws Exception if signature generation fails
     */
    private HttpHeaders createAuthHeaders(String method, String uri, Map<String, Object> payload) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        
        // Generate random string for authentication
        String randomString = UUID.randomUUID().toString();
        
        // Create authorization string
        String payloadString = objectMapper.writeValueAsString(payload);
        String authString = apiKey + randomString + payloadString;
        String authorization = generateSignature(authString);
        
        headers.set("Authorization", "IYZWS " + apiKey + ":" + authorization);
        headers.set("x-iyzi-rnd", randomString);
        
        return headers;
    }

    /**
     * Generate HMAC signature for authentication.
     * 
     * @param data data to sign
     * @return base64 encoded signature
     * @throws NoSuchAlgorithmException if HMAC algorithm is not available
     * @throws InvalidKeyException if secret key is invalid
     */
    private String generateSignature(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Parse payment response from IyziLink API.
     * 
     * @param responseBody response body
     * @return payment response
     * @throws Exception if parsing fails
     */
    private PaymentResponse parsePaymentResponse(String responseBody) throws Exception {
        JsonNode response = objectMapper.readTree(responseBody);
        
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setSuccess("success".equals(response.path("status").asText()));
        paymentResponse.setProviderPaymentId(response.path("paymentId").asText());
        paymentResponse.setStatus(response.path("status").asText());
        paymentResponse.setErrorMessage(response.path("errorMessage").asText());
        paymentResponse.setErrorCode(response.path("errorCode").asText());
        paymentResponse.setAuthCode(response.path("authCode").asText());
        paymentResponse.setHostReference(response.path("hostReference").asText());
        
        // Extract card information if available
        JsonNode paymentCard = response.path("paymentCard");
        if (!paymentCard.isMissingNode()) {
            paymentResponse.setCardLastFour(paymentCard.path("cardLastFour").asText());
            paymentResponse.setCardBrand(paymentCard.path("cardFamily").asText());
        }
        
        return paymentResponse;
    }

    /**
     * Parse refund response from IyziLink API.
     * 
     * @param responseBody response body
     * @return refund response
     * @throws Exception if parsing fails
     */
    private RefundResponse parseRefundResponse(String responseBody) throws Exception {
        JsonNode response = objectMapper.readTree(responseBody);
        
        RefundResponse refundResponse = new RefundResponse();
        refundResponse.setSuccess("success".equals(response.path("status").asText()));
        refundResponse.setRefundId(response.path("paymentTransactionId").asText());
        refundResponse.setStatus(response.path("status").asText());
        refundResponse.setErrorMessage(response.path("errorMessage").asText());
        refundResponse.setErrorCode(response.path("errorCode").asText());
        
        return refundResponse;
    }
}