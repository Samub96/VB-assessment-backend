package com.assesment.backpayments.application.dto;

import java.math.BigDecimal;

/**
 * Datos mínimos para crear una orden de pago.
 */
public record CreateOrderRequest(BigDecimal amount, String currency, String description) {
}
