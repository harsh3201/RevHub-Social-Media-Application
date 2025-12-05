package com.example.revHubBack.service;

import com.example.revHubBack.entity.Follow;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.FollowRepository;
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

class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMongoService notificationService;

    @InjectMocks
    private FollowService followService;

    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        follower = new User();
        follower.setId(1L);
        follower.setUsername("follower");
        
        following = new User();
        following.setId(2L);
        following.setUsername("following");
    }

    @Test
    void followUser_Success() {
        following.setIsPrivate(false);
        when(userRepository.findByUsername("follower")).thenReturn(Optional.of(follower));
        when(userRepository.findByUsername("following")).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.empty());

        String result = followService.followUser("follower", "following");

        assertTrue(result.contains("Now following"));
        verify(followRepository).save(any(Follow.class));
        verify(notificationService).createFollowNotification(following, follower);
    }

    @Test
    void followUser_PrivateAccount() {
        following.setIsPrivate(true);
        when(userRepository.findByUsername("follower")).thenReturn(Optional.of(follower));
        when(userRepository.findByUsername("following")).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.empty());
        when(followRepository.save(any(Follow.class))).thenReturn(new Follow());

        String result = followService.followUser("follower", "following");

        assertEquals("Follow request sent", result);
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void unfollowUser_Success() {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        
        when(userRepository.findByUsername("follower")).thenReturn(Optional.of(follower));
        when(userRepository.findByUsername("following")).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.of(follow));

        assertDoesNotThrow(() -> followService.unfollowUser("follower", "following"));
        verify(followRepository).delete(follow);
    }

    @Test
    void getFollowStatus_Success() {
        when(userRepository.findByUsername("follower")).thenReturn(Optional.of(follower));
        when(userRepository.findByUsername("following")).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.empty());

        String result = followService.getFollowStatus("follower", "following");

        assertEquals("NOT_FOLLOWING", result);
    }
}