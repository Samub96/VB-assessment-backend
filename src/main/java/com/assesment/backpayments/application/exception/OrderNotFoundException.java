package com.assesment.backpayments.application.exception;

/**
 * Se lanza cuando una orden no existe.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
