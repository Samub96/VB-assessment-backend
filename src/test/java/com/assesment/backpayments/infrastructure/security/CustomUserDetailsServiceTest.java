package com.assesment.backpayments.infrastructure.security;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loadsAuthoritiesWithoutLazyInitializationError() {
        UUID userId = UUID.randomUUID();
        String email = "lazy-test@" + userId + ".local";

        jdbcTemplate.update("INSERT INTO users (id, email, password_hash) VALUES (?,?,?)", userId.toString(), email, "hash");
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role_name) VALUES (?, ?)", userId.toString(), "ADMIN");

        SecurityUserDetails details = (SecurityUserDetails) userDetailsService.loadUserByUsername(email);

        assertEquals(1, details.getAuthorities().size());
        assertEquals("ROLE_ADMIN", details.getAuthorities().iterator().next().getAuthority());
    }
}
