package com.example.revHubBack.service;

import com.example.revHubBack.entity.PasswordResetToken;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.PasswordResetTokenRepository;
import com.example.revHubBack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@email.com");
        
        testToken = new PasswordResetToken();
        testToken.setToken("test-token");
        testToken.setUser(testUser);
        testToken.setUsed(false);
        testToken.setExpiryDate(LocalDateTime.now().plusHours(1));
    }

    @Test
    void createPasswordResetToken_Success() throws Exception {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        String result = passwordResetService.createPasswordResetToken("test@email.com");

        assertTrue(result.contains("Password reset email sent successfully"));
        verify(emailService).sendPasswordResetEmail(eq("test@email.com"), anyString());
    }

    @Test
    void createPasswordResetToken_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@email.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            passwordResetService.createPasswordResetToken("nonexistent@email.com"));
    }

    @Test
    void resetPassword_Success() {
        when(tokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        String result = passwordResetService.resetPassword("test-token", "newpassword");

        assertEquals("Password reset successfully", result);
        assertTrue(testToken.getUsed());
        verify(userRepository).save(testUser);
    }

    @Test
    void resetPassword_ExpiredToken() {
        testToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(testToken));

        assertThrows(RuntimeException.class, () -> 
            passwordResetService.resetPassword("expired-token", "newpassword"));
    }
}