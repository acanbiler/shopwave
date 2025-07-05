package com.acanbiler.shopwave.service.payment;

/**
 * Exception thrown when payment processing fails.
 * 
 * This exception provides detailed information about payment processing
 * failures including error codes and provider-specific error messages.
 */
public class PaymentProcessingException extends Exception {

    private final String errorCode;
    private final String providerErrorCode;
    private final String providerErrorMessage;

    /**
     * Constructs a new payment processing exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public PaymentProcessingException(String message) {
        super(message);
        this.errorCode = null;
        this.providerErrorCode = null;
        this.providerErrorMessage = null;
    }

    /**
     * Constructs a new payment processing exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.providerErrorCode = null;
        this.providerErrorMessage = null;
    }

    /**
     * Constructs a new payment processing exception with detailed error information.
     * 
     * @param message the detail message
     * @param errorCode application-specific error code
     * @param providerErrorCode provider-specific error code
     * @param providerErrorMessage provider-specific error message
     */
    public PaymentProcessingException(String message, String errorCode, String providerErrorCode, String providerErrorMessage) {
        super(message);
        this.errorCode = errorCode;
        this.providerErrorCode = providerErrorCode;
        this.providerErrorMessage = providerErrorMessage;
    }

    /**
     * Constructs a new payment processing exception with detailed error information and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     * @param errorCode application-specific error code
     * @param providerErrorCode provider-specific error code
     * @param providerErrorMessage provider-specific error message
     */
    public PaymentProcessingException(String message, Throwable cause, String errorCode, String providerErrorCode, String providerErrorMessage) {
        super(message, cause);
        this.errorCode = errorCode;
        this.providerErrorCode = providerErrorCode;
        this.providerErrorMessage = providerErrorMessage;
    }

    /**
     * Gets the application-specific error code.
     * 
     * @return error code or null if not available
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the provider-specific error code.
     * 
     * @return provider error code or null if not available
     */
    public String getProviderErrorCode() {
        return providerErrorCode;
    }

    /**
     * Gets the provider-specific error message.
     * 
     * @return provider error message or null if not available
     */
    public String getProviderErrorMessage() {
        return providerErrorMessage;
    }

    /**
     * Returns a detailed error message including provider information.
     * 
     * @return detailed error message
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(getMessage());
        
        if (errorCode != null) {
            sb.append(" [Error Code: ").append(errorCode).append("]");
        }
        
        if (providerErrorCode != null) {
            sb.append(" [Provider Error: ").append(providerErrorCode);
            if (providerErrorMessage != null) {
                sb.append(" - ").append(providerErrorMessage);
            }
            sb.append("]");
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return "PaymentProcessingException{" +
                "message='" + getMessage() + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", providerErrorCode='" + providerErrorCode + '\'' +
                ", providerErrorMessage='" + providerErrorMessage + '\'' +
                '}';
    }
}