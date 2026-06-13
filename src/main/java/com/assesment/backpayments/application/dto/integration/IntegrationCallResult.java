package com.assesment.backpayments.application.dto.integration;

import java.time.Instant;

/**
 * Resultado de una llamada de integración externa.
 */
public record IntegrationCallResult(
        boolean success,
        int statusCode,
        String requestBody,
        String responseBody,
        String errorMessage,
        Instant executedAt
) {
}
