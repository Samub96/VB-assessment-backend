package com.assesment.backpayments.application.dto.integration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Payload enviado al servicio externo dummy al aprobar una orden.
 */
public record ExternalApprovalNotificationRequest(
        UUID orderId,
        String status,
        BigDecimal amount,
        String currency,
        String description,
        Instant approvedAt,
        String approvedBy
) {
}
