package com.assesment.backpayments.application.dto;

import com.assesment.backpayments.domain.model.RoleName;
import java.util.Set;
import java.util.UUID;

/**
 * Información básica del usuario expuesta al cliente.
 */
public record UserResponse(UUID id, String email, Set<RoleName> roles) {
}
