package com.assesment.backpayments.application.service;

import com.assesment.backpayments.application.dto.AuthRequest;
import com.assesment.backpayments.application.dto.AuthResponse;
import com.assesment.backpayments.infrastructure.security.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void loginReturnsRolesInResponse() {
        AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
        JwtTokenProvider tokenProvider = Mockito.mock(JwtTokenProvider.class);
        AuthService authService = new AuthService(authenticationManager, tokenProvider);

        UserDetails userDetails = new User(
                "admin@local",
                "hash",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(userDetails)).thenReturn("jwt-token");

        AuthResponse response = authService.login(new AuthRequest("admin@local", "Admin123!"));

        assertEquals("jwt-token", response.token());
        assertEquals(List.of("ROLE_ADMIN"), response.roles());
    }
}
