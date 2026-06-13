package com.assesment.backpayments.infrastructure.integration;

import com.assesment.backpayments.application.dto.integration.IntegrationCallResult;
import com.assesment.backpayments.application.event.OrderApprovedEvent;
import com.assesment.backpayments.application.service.OrderIntegrationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Dispara la integración externa solo después de confirmar la aprobación.
 */
@Component
public class OrderIntegrationListener {

    private final OrderIntegrationService orderIntegrationService;

    public OrderIntegrationListener(OrderIntegrationService orderIntegrationService) {
        this.orderIntegrationService = orderIntegrationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderApproved(OrderApprovedEvent event) {
        IntegrationCallResult result = orderIntegrationService.notifyOrderApproved(event);
        if (!result.success()) {
            return;
        }
    }
}
