package com.assesment.backpayments.infrastructure.config;

import com.assesment.backpayments.domain.model.Role;
import com.assesment.backpayments.domain.model.RoleName;
import com.assesment.backpayments.domain.model.User;
import com.assesment.backpayments.infrastructure.storage.RoleRepository;
import com.assesment.backpayments.infrastructure.storage.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSeederTest {

    @Test
    void runRepairsExistingAdminRole() {
        RoleRepository roleRepository = mock(RoleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        DataSeeder dataSeeder = new DataSeeder(roleRepository, userRepository, passwordEncoder);

        ReflectionTestUtils.setField(dataSeeder, "enabled", true);
        ReflectionTestUtils.setField(dataSeeder, "adminEmail", "admin@local");
        ReflectionTestUtils.setField(dataSeeder, "adminPassword", "Admin123!");
        ReflectionTestUtils.setField(dataSeeder, "operatorEmail", "operator@local");
        ReflectionTestUtils.setField(dataSeeder, "operatorPassword", "Operator123!");

        Role adminRole = new Role(RoleName.ADMIN);
        Role operatorRole = new Role(RoleName.OPERATOR);
        when(roleRepository.findById(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findById(RoleName.OPERATOR)).thenReturn(Optional.of(operatorRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        User existingAdmin = new User();
        existingAdmin.setEmail("admin@local");
        existingAdmin.setRoles(Set.of(operatorRole));
        when(userRepository.findByEmail("admin@local")).thenReturn(Optional.of(existingAdmin));
        when(userRepository.findByEmail("operator@local")).thenReturn(Optional.empty());

        dataSeeder.run(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(userCaptor.capture());

        User savedAdmin = userCaptor.getAllValues().get(0);
        assertEquals("admin@local", savedAdmin.getEmail());
        assertTrue(savedAdmin.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ADMIN));
    }
}
