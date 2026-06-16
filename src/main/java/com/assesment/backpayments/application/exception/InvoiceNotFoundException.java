package com.assesment.backpayments.application.exception;

/**
 * Se lanza cuando no existe una factura almacenada para descargar.
 */
public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(String message) {
        super(message);
    }
}
