package com.assesment.backpayments.application.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio emitido cuando una orden queda aprobada.
 */
public record OrderApprovedEvent(
        UUID orderId,
        BigDecimal amount,
        String currency,
        String description,
        Instant approvedAt,
        String approvedBy
) {
}
