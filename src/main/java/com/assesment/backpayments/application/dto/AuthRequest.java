package com.assesment.backpayments.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de inicio de sesión.
 */
public record AuthRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
