package com.devcollab.service;

import com.devcollab.dto.AuthResponse;
import com.devcollab.dto.RegisterRequest;
import com.devcollab.entity.User;
import com.devcollab.exception.DuplicateResourceException;
import com.devcollab.repository.UserRepository;
import com.devcollab.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("alice");
        registerRequest.setEmail("alice@example.com");
        registerRequest.setPassword("password123");
    }

    @Test
    void register_throwsDuplicateResourceException_whenUsernameTaken() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username");

        // Guard against a regression where the account gets created anyway before
        // the duplicate check short-circuits.
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_throwsDuplicateResourceException_whenEmailTaken() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_hashesPasswordAndReturnsToken_onSuccess() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        UserDetails mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("alice").password("hashed-password").authorities("ROLE_USER").build();
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(mockUserDetails);
        when(jwtService.generateToken(mockUserDetails)).thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getUsername()).isEqualTo("alice");

        // The raw password must never reach the repository - only the hash should.
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("hashed-password") && !user.getPassword().equals("password123")));
    }
}
