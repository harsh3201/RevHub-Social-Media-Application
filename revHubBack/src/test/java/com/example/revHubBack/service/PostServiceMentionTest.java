package com.example.revHubBack.service;

import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.PostRepository;
import com.example.revHubBack.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostServiceMentionTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMongoService notificationService;

    @Mock
    private NotificationService notificationServiceSQL;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMentionProcessing() {
        // Setup test data
        User author = new User();
        author.setId(1L);
        author.setUsername("author");

        User mentionedUser = new User();
        mentionedUser.setId(2L);
        mentionedUser.setUsername("testuser");

        Post post = new Post();
        post.setId(1L);
        post.setContent("Hello @testuser, how are you?");
        post.setAuthor(author);

        // Mock repository calls
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mentionedUser));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // Execute
        Post result = postService.createPost("Hello @testuser, how are you?", null, "author", "PUBLIC");

        // Verify notifications were created
        verify(notificationService, times(1)).createMentionNotification(
            eq(mentionedUser), eq(author), eq(1L), eq("Hello @testuser, how are you?"));
        verify(notificationServiceSQL, times(1)).createMentionNotification(
            eq(mentionedUser), eq(author), eq(1L));
    }

    @Test
    void testMultipleMentions() {
        // Setup test data
        User author = new User();
        author.setId(1L);
        author.setUsername("author");

        User user1 = new User();
        user1.setId(2L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(3L);
        user2.setUsername("user2");

        Post post = new Post();
        post.setId(1L);
        post.setContent("Hello @user1 and @user2!");
        post.setAuthor(author);

        // Mock repository calls
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(user2));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // Execute
        postService.createPost("Hello @user1 and @user2!", null, "author", "PUBLIC");

        // Verify both users got notifications
        verify(notificationService, times(1)).createMentionNotification(
            eq(user1), eq(author), eq(1L), eq("Hello @user1 and @user2!"));
        verify(notificationService, times(1)).createMentionNotification(
            eq(user2), eq(author), eq(1L), eq("Hello @user1 and @user2!"));
    }

    @Test
    void testSelfMentionIgnored() {
        // Setup test data
        User author = new User();
        author.setId(1L);
        author.setUsername("author");

        Post post = new Post();
        post.setId(1L);
        post.setContent("Hello @author, testing self mention");
        post.setAuthor(author);

        // Mock repository calls
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // Execute
        postService.createPost("Hello @author, testing self mention", null, "author", "PUBLIC");

        // Verify no notification was created for self-mention
        verify(notificationService, never()).createMentionNotification(any(), any(), any(), any());
        verify(notificationServiceSQL, never()).createMentionNotification(any(), any(), any());
    }
}