package com.example.revHubBack.service;

import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private User testUser;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@email.com");
        testUser.setIsVerified(false);
    }

    @Test
    void sendVerificationOTP_Success() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(testUser));

        String result = emailVerificationService.sendVerificationOTP("test@email.com");

        assertTrue(result.contains("Verification OTP sent successfully"));
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void sendVerificationOTP_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@email.com")).thenReturn(Optional.empty());

        String result = emailVerificationService.sendVerificationOTP("nonexistent@email.com");

        assertEquals("User not found", result);
    }

    @Test
    void verifyOTP_Success() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        emailVerificationService.sendVerificationOTP("test@email.com");
        String result = emailVerificationService.verifyOTP("test@email.com", "123456");

        assertNotNull(result);
    }

    @Test
    void verifyEmail_Success() {
        testUser.setVerificationToken("test-token");
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String result = emailVerificationService.verifyEmail("test-token");

        assertEquals("Email verified successfully", result);
        assertTrue(testUser.getIsVerified());
    }
}