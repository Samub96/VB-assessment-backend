package com.assesment.backpayments.infrastructure.integration;

import com.assesment.backpayments.application.dto.integration.IntegrationCallResult;
import com.assesment.backpayments.application.event.OrderApprovedEvent;
import com.assesment.backpayments.application.service.OrderIntegrationService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderIntegrationListenerTest {

    @Test
    void onOrderApprovedDelegatesToService() {
        OrderIntegrationService service = Mockito.mock(OrderIntegrationService.class);
        OrderIntegrationListener listener = new OrderIntegrationListener(service);
        OrderApprovedEvent event = new OrderApprovedEvent(
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "USD",
                "desc",
                Instant.now(),
                "admin@local"
        );
        when(service.notifyOrderApproved(event))
                .thenReturn(new IntegrationCallResult(true, 200, "{}", "{}", null, Instant.now()));

        listener.onOrderApproved(event);

        verify(service, times(1)).notifyOrderApproved(event);
    }
}
