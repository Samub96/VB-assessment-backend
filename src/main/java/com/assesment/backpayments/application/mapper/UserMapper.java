package com.assesment.backpayments.application.mapper;

import com.assesment.backpayments.application.dto.UserResponse;
import com.assesment.backpayments.domain.model.Role;
import com.assesment.backpayments.domain.model.RoleName;
import com.assesment.backpayments.domain.model.User;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Conversión entre entidades de usuario y DTOs públicos.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        Set<RoleName> roles = user.getRoles() == null
                ? Collections.emptySet()
                : user.getRoles().stream().map(Role::getName).collect(Collectors.toUnmodifiableSet());
        return new UserResponse(user.getId(), user.getEmail(), roles);
    }
}
