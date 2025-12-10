package com.example.revHubBack.service;

import com.example.revHubBack.entity.Notification;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.NotificationRepository;
import com.example.revHubBack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUser(testUser);
        testNotification.setReadStatus(false);
    }

    @Test
    void getUserNotifications_Success() {
        List<Notification> notifications = Arrays.asList(testNotification);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByUserOrderByCreatedDateDesc(testUser)).thenReturn(notifications);

        List<Notification> result = notificationService.getUserNotifications("testuser");

        assertEquals(1, result.size());
        assertEquals(testNotification, result.get(0));
    }

    @Test
    void getUserNotifications_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            notificationService.getUserNotifications("nonexistent"));
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        Notification result = notificationService.markAsRead(1L, "testuser");

        assertTrue(result.getReadStatus());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void markAsRead_Unauthorized() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        assertThrows(RuntimeException.class, () -> 
            notificationService.markAsRead(1L, "otheruser"));
    }
}