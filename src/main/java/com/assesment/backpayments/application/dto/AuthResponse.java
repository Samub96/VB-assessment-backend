package com.assesment.backpayments.application.dto;

import java.time.Instant;
import java.util.List;

/**
 * Respuesta de autenticación con token JWT.
 */
public record AuthResponse(String token, String tokenType, Instant expiresAt, List<String> roles) {
}
