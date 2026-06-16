package com.assesment.backpayments.application.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos mínimos para crear una orden de pago.
 */
public record CreateOrderRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @Size(max = 255) String description
) {
}
