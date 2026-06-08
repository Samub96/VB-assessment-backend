package com.assesment.backpayments.application.dto;

import java.time.Instant;

/**
 * Respuesta de autenticación con token JWT.
 */
public record AuthResponse(String token, String tokenType, Instant expiresAt) {
}
