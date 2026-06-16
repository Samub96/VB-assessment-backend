package com.assesment.backpayments.infrastructure.web;

import com.assesment.backpayments.application.exception.IncorrectInvoiceException;
import com.assesment.backpayments.application.exception.InvalidOrderStateException;
import com.assesment.backpayments.application.exception.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsOrderNotFoundTo404() {
        ProblemDetail detail = handler.handleOrderNotFound(new OrderNotFoundException("not found"));
        assertEquals(HttpStatus.NOT_FOUND.value(), detail.getStatus());
        assertEquals("not found", detail.getDetail());
    }

    @Test
    void mapsInvalidStateTo409() {
        ProblemDetail detail = handler.handleInvalidState(new InvalidOrderStateException("invalid"));
        assertEquals(HttpStatus.CONFLICT.value(), detail.getStatus());
        assertEquals("invalid", detail.getDetail());
    }

    @Test
    void mapsInvoiceErrorTo400() {
        ProblemDetail detail = handler.handleInvoiceError(new IncorrectInvoiceException("bad invoice"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), detail.getStatus());
        assertEquals("bad invoice", detail.getDetail());
    }

    @Test
    void mapsAccessDeniedTo403() {
        ProblemDetail detail = handler.handleAccessDenied(new AccessDeniedException("forbidden"));
        assertEquals(HttpStatus.FORBIDDEN.value(), detail.getStatus());
        assertEquals("forbidden", detail.getDetail());
    }
}
