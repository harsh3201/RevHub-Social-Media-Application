package com.example.revHubBack.service;

import com.example.revHubBack.dto.JwtResponse;
import com.example.revHubBack.dto.LoginRequest;
import com.example.revHubBack.dto.RegisterRequest;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.UserRepository;
import com.example.revHubBack.security.JwtUtils;
import com.example.revHubBack.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticateUser_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        Authentication auth = mock(Authentication.class);
        UserPrincipal userPrincipal = new UserPrincipal(1L, "testuser", "test@email.com", "password");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userPrincipal);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("jwt-token");

        JwtResponse result = authService.authenticateUser(loginRequest);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void registerUser_Success() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@email.com");
        registerRequest.setPassword("password");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);
        when(encoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        String result = authService.registerUser(registerRequest);

        assertTrue(result.contains("registered successfully"));
        verify(emailVerificationService).sendVerificationOTP("new@email.com");
    }

    @Test
    void registerUser_UsernameExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existinguser");
        registerRequest.setEmail("new@email.com");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        String result = authService.registerUser(registerRequest);

        assertEquals("Error: Username is already taken!", result);
    }

    @Test
    void registerUser_EmailExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("existing@email.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

        String result = authService.registerUser(registerRequest);

        assertEquals("Error: Email is already in use!", result);
    }
}