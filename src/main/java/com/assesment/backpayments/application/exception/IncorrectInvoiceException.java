package com.assesment.backpayments.application.exception;

/**
 * Indica que la factura enviada no cumple el contrato esperado.
 */
public class IncorrectInvoiceException extends RuntimeException {

    public IncorrectInvoiceException(String message) {
        super(message);
    }

    public IncorrectInvoiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
