package com.assesment.backpayments.infrastructure.web;

import com.assesment.backpayments.application.exception.IncorrectInvoiceException;
import com.assesment.backpayments.application.exception.InvoiceNotFoundException;
import com.assesment.backpayments.application.exception.InvalidOrderStateException;
import com.assesment.backpayments.application.exception.OrderNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce errores funcionales a respuestas HTTP coherentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFound(OrderNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detail.setDetail(ex.getMessage());
        return detail;
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ProblemDetail handleInvalidState(InvalidOrderStateException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        detail.setDetail(ex.getMessage());
        return detail;
    }

    @ExceptionHandler(IncorrectInvoiceException.class)
    public ProblemDetail handleInvoiceError(IncorrectInvoiceException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setDetail(ex.getMessage());
        return detail;
    }

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ProblemDetail handleInvoiceNotFound(InvoiceNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detail.setDetail(ex.getMessage());
        return detail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        detail.setDetail(ex.getMessage());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setDetail("Error de validación");
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setDetail(ex.getMessage());
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setDetail(ex.getMessage());
        return detail;
    }
}
