package com.assesment.backpayments.application.dto;

import com.assesment.backpayments.domain.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representación resumida para listados de órdenes archivadas.
 */
public record ArchivedOrderSummaryResponse(
        UUID id,
        OrderStatus status,
        BigDecimal amount,
        String currency,
        Instant createdAt,
        Instant archivedAt
) {
}
