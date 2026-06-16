package com.assesment.backpayments.application.service;

import com.assesment.backpayments.application.dto.integration.ExternalApprovalNotificationRequest;
import com.assesment.backpayments.application.dto.integration.IntegrationCallResult;
import com.assesment.backpayments.application.exception.ExternalIntegrationClientException;
import com.assesment.backpayments.application.event.OrderApprovedEvent;
import com.assesment.backpayments.domain.model.IntegrationResponseLog;
import com.assesment.backpayments.infrastructure.integration.DummyIntegrationClient;
import com.assesment.backpayments.infrastructure.storage.IntegrationResponseLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Orquesta la notificación de órdenes aprobadas al servicio externo dummy.
 */
@Service
public class OrderIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(OrderIntegrationService.class);

    private final DummyIntegrationClient client;
    private final IntegrationResponseLogRepository logRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final boolean persistResponse;
    private final String providerName;

    public OrderIntegrationService(
            DummyIntegrationClient client,
            IntegrationResponseLogRepository logRepository,
            @Value("${app.integration.persist-response:true}") boolean persistResponse,
            @Value("${app.integration.provider-name:dummy-external}") String providerName
    ) {
        this.client = client;
        this.logRepository = logRepository;
        this.persistResponse = persistResponse;
        this.providerName = providerName;
    }

    @Retry(name = "dummyIntegration")
    @CircuitBreaker(name = "dummyIntegration", fallbackMethod = "handleFailure")
    public IntegrationCallResult notifyOrderApproved(OrderApprovedEvent event) {
        ExternalApprovalNotificationRequest request = toRequest(event);
        String requestBody = serialize(request);
        Map<String, Object> response = client.sendApproval(request);
        String responseBody = serialize(response);
        persistLog(event, true, 200, requestBody, responseBody, null);
        log.info("Integración externa exitosa para orden {}", event.orderId());
        return new IntegrationCallResult(true, 200, requestBody, responseBody, null, Instant.now());
    }

    public IntegrationCallResult handleFailure(OrderApprovedEvent event, Throwable throwable) {
        ExternalApprovalNotificationRequest request = toRequest(event);
        String requestBody = serialize(request);

        int statusCode = 503;
        String responseBody = null;
        String errorMessage = throwable.getMessage();

        if (throwable instanceof ExternalIntegrationClientException external) {
            statusCode = external.getStatusCode();
            responseBody = external.getResponseBody();
            if (!StringUtils.hasText(errorMessage)) {
                errorMessage = "Error HTTP " + statusCode;
            }
        } else if (throwable instanceof RetryableException) {
            errorMessage = "Timeout o error de red al llamar al servicio externo";
        }

        persistLog(event, false, statusCode, requestBody, responseBody, errorMessage);
        log.warn("Integración externa fallida para orden {}: {}", event.orderId(), errorMessage);
        return new IntegrationCallResult(false, statusCode, requestBody, responseBody, errorMessage, Instant.now());
    }

    private ExternalApprovalNotificationRequest toRequest(OrderApprovedEvent event) {
        return new ExternalApprovalNotificationRequest(
                event.orderId(),
                "APPROVED",
                event.amount(),
                event.currency(),
                event.description(),
                event.approvedAt(),
                event.approvedBy()
        );
    }

    private void persistLog(
            OrderApprovedEvent event,
            boolean success,
            int statusCode,
            String requestBody,
            String responseBody,
            String errorMessage
    ) {
        if (!persistResponse) {
            return;
        }
        IntegrationResponseLog logEntry = new IntegrationResponseLog();
        logEntry.setOrderId(event.orderId());
        logEntry.setProvider(providerName);
        logEntry.setSuccess(success);
        logEntry.setStatusCode(statusCode);
        logEntry.setRequestBody(truncate(requestBody, 4000));
        logEntry.setResponseBody(truncate(responseBody, 8000));
        logEntry.setErrorMessage(truncate(errorMessage, 2000));
        logEntry.setExecutedAt(Instant.now());
        logRepository.save(logEntry);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{\"serializationError\":\"" + ex.getOriginalMessage() + "\"}";
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
