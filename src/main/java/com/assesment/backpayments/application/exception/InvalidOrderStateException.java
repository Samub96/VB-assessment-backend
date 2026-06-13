package com.assesment.backpayments.application.exception;

/**
 * Se lanza cuando una transición de estado no está permitida.
 */
public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
