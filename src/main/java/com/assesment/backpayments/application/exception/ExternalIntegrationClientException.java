package com.assesment.backpayments.application.exception;

/**
 * Se lanza cuando el servicio externo responde con error HTTP.
 */
public class ExternalIntegrationClientException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public ExternalIntegrationClientException(int statusCode, String message, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
