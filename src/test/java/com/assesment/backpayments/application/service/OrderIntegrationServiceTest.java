package com.assesment.backpayments.application.service;

import com.assesment.backpayments.application.dto.integration.IntegrationCallResult;
import com.assesment.backpayments.application.event.OrderApprovedEvent;
import com.assesment.backpayments.application.exception.ExternalIntegrationClientException;
import com.assesment.backpayments.domain.model.IntegrationResponseLog;
import com.assesment.backpayments.infrastructure.integration.DummyIntegrationClient;
import com.assesment.backpayments.infrastructure.storage.IntegrationResponseLogRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderIntegrationServiceTest {

    @Test
    void notifyOrderApprovedSuccessPersistsLog() {
        DummyIntegrationClient client = Mockito.mock(DummyIntegrationClient.class);
        IntegrationResponseLogRepository repository = Mockito.mock(IntegrationResponseLogRepository.class);
        OrderIntegrationService service = new OrderIntegrationService(client, repository, true, "dummy");
        OrderApprovedEvent event = sampleEvent();

        when(client.sendApproval(any())).thenReturn(Map.of("ok", true));

        IntegrationCallResult result = service.notifyOrderApproved(event);

        assertTrue(result.success());
        assertEquals(200, result.statusCode());
        ArgumentCaptor<IntegrationResponseLog> captor = ArgumentCaptor.forClass(IntegrationResponseLog.class);
        verify(repository, times(1)).save(captor.capture());
        assertTrue(captor.getValue().isSuccess());
        assertEquals(200, captor.getValue().getStatusCode());
    }

    @Test
    void handleFailureWithHttpErrorUsesStatusAndBody() {
        DummyIntegrationClient client = Mockito.mock(DummyIntegrationClient.class);
        IntegrationResponseLogRepository repository = Mockito.mock(IntegrationResponseLogRepository.class);
        OrderIntegrationService service = new OrderIntegrationService(client, repository, true, "dummy");
        OrderApprovedEvent event = sampleEvent();

        ExternalIntegrationClientException ex =
                new ExternalIntegrationClientException(500, "error", "{\"error\":\"down\"}");

        IntegrationCallResult result = service.handleFailure(event, ex);

        assertFalse(result.success());
        assertEquals(500, result.statusCode());
        assertEquals("{\"error\":\"down\"}", result.responseBody());
        verify(repository, times(1)).save(any(IntegrationResponseLog.class));
    }

    @Test
    void notifyOrderApprovedDoesNotPersistWhenDisabled() {
        DummyIntegrationClient client = Mockito.mock(DummyIntegrationClient.class);
        IntegrationResponseLogRepository repository = Mockito.mock(IntegrationResponseLogRepository.class);
        OrderIntegrationService service = new OrderIntegrationService(client, repository, false, "dummy");

        when(client.sendApproval(any())).thenReturn(Map.of("ok", true));
        service.notifyOrderApproved(sampleEvent());

        verify(repository, never()).save(any());
    }

    private static OrderApprovedEvent sampleEvent() {
        return new OrderApprovedEvent(
                UUID.randomUUID(),
                new BigDecimal("55.00"),
                "USD",
                "desc",
                Instant.now(),
                "admin@local"
        );
    }
}
