package com.assesment.backpayments.application.dto;

import com.assesment.backpayments.domain.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representación de una orden expuesta al consumidor del API.
 */
public record OrderResponse(
        UUID id,
        OrderStatus status,
        BigDecimal amount,
        String currency,
        String description,
        String invoiceKey,
        Instant createdAt,
        UUID createdById
) {
}
