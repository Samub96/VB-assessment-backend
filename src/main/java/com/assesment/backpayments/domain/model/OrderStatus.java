package com.assesment.backpayments.domain.model;

/**
 * Estados válidos del ciclo de vida de una orden de pago.
 */
public enum OrderStatus {
    PENDING,
    APPROVED,
    REJECTED;
}
