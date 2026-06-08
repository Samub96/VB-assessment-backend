package com.assesment.backpayments.infrastructure.config;

import com.assesment.backpayments.domain.model.Role;
import com.assesment.backpayments.domain.model.RoleName;
import com.assesment.backpayments.domain.model.User;
import com.assesment.backpayments.infrastructure.storage.RoleRepository;
import com.assesment.backpayments.infrastructure.storage.UserRepository;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga usuarios y roles mínimos para pruebas locales.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean enabled;

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Value("${app.seed.operator.email}")
    private String operatorEmail;

    @Value("${app.seed.operator.password}")
    private String operatorPassword;

    public DataSeeder(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        Role adminRole = roleRepository.findById(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN)));
        Role operatorRole = roleRepository.findById(RoleName.OPERATOR)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.OPERATOR)));

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail(operatorEmail)) {
            User operator = new User();
            operator.setEmail(operatorEmail);
            operator.setPasswordHash(passwordEncoder.encode(operatorPassword));
            operator.setRoles(Set.of(operatorRole));
            userRepository.save(operator);
        }
    }
}
