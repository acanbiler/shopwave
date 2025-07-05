package com.acanbiler.shopwave.config;

import com.acanbiler.shopwave.service.payment.IyziLinkPaymentProvider;
import com.acanbiler.shopwave.service.payment.PaymentProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Payment configuration for ShopWave application.
 * 
 * Configures payment providers with proper settings and manages
 * provider registry for processing payments through different providers.
 */
@Configuration
@ConfigurationProperties(prefix = "shopwave.payment")
public class PaymentConfig {

    /**
     * Payment provider configurations.
     */
    private final Map<String, ProviderConfig> providers = new ConcurrentHashMap<>();

    /**
     * Default payment provider.
     */
    private String defaultProvider = "iyzico";

    /**
     * Payment processing timeout in seconds.
     */
    private int timeoutSeconds = 30;

    /**
     * Webhook verification timeout in seconds.
     */
    private int webhookTimeoutSeconds = 10;

    /**
     * Maximum retry attempts for failed payments.
     */
    private int maxRetryAttempts = 3;

    /**
     * RetryDelay between payment attempts in seconds.
     */
    private int retryDelaySeconds = 5;

    /**
     * RestTemplate for HTTP communication with payment providers.
     */
    @Bean
    @Primary
    public RestTemplate paymentRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // Configure timeout and error handling
        return restTemplate;
    }

    /**
     * IyziLink payment provider bean.
     */
    @Bean
    public PaymentProvider iyziLinkPaymentProvider(RestTemplate paymentRestTemplate) {
        ProviderConfig config = providers.getOrDefault("iyzico", new ProviderConfig());
        return new IyziLinkPaymentProvider(
            config.getApiKey(),
            config.getSecretKey(),
            config.getApiUrl(),
            config.isTestMode(),
            paymentRestTemplate
        );
    }

    /**
     * Payment provider registry for managing multiple providers.
     */
    @Bean
    public PaymentProviderRegistry paymentProviderRegistry(PaymentProvider iyziLinkPaymentProvider) {
        PaymentProviderRegistry registry = new PaymentProviderRegistry();
        registry.registerProvider("IYZILINK", iyziLinkPaymentProvider);
        registry.setDefaultProvider("IYZILINK");
        return registry;
    }

    /**
     * Payment provider registry implementation.
     */
    public static class PaymentProviderRegistry {
        private final Map<String, PaymentProvider> providers = new ConcurrentHashMap<>();
        private String defaultProvider;

        public void registerProvider(String name, PaymentProvider provider) {
            providers.put(name.toUpperCase(), provider);
        }

        public PaymentProvider getProvider(String name) {
            return providers.get(name.toUpperCase());
        }

        public PaymentProvider getDefaultProvider() {
            return defaultProvider != null ? providers.get(defaultProvider.toUpperCase()) : null;
        }

        public void setDefaultProvider(String defaultProvider) {
            this.defaultProvider = defaultProvider;
        }

        public Map<String, PaymentProvider> getAllProviders() {
            return Map.copyOf(providers);
        }
    }

    /**
     * Provider configuration class.
     */
    public static class ProviderConfig {
        private String apiKey;
        private String secretKey;
        private String apiUrl;
        private boolean testMode = true;
        private String webhookSecret;
        private int timeoutSeconds = 30;

        // Constructors
        public ProviderConfig() {}

        public ProviderConfig(String apiKey, String secretKey, String apiUrl, boolean testMode) {
            this.apiKey = apiKey;
            this.secretKey = secretKey;
            this.apiUrl = apiUrl;
            this.testMode = testMode;
        }

        // Getters and setters
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

        public boolean isTestMode() { return testMode; }
        public void setTestMode(boolean testMode) { this.testMode = testMode; }

        public String getWebhookSecret() { return webhookSecret; }
        public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }

        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }

    // Getters and setters for configuration properties
    public Map<String, ProviderConfig> getProviders() { return providers; }

    public String getDefaultProvider() { return defaultProvider; }
    public void setDefaultProvider(String defaultProvider) { this.defaultProvider = defaultProvider; }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

    public int getWebhookTimeoutSeconds() { return webhookTimeoutSeconds; }
    public void setWebhookTimeoutSeconds(int webhookTimeoutSeconds) { this.webhookTimeoutSeconds = webhookTimeoutSeconds; }

    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }

    public int getRetryDelaySeconds() { return retryDelaySeconds; }
    public void setRetryDelaySeconds(int retryDelaySeconds) { this.retryDelaySeconds = retryDelaySeconds; }
}