package com.example.revHubBack.service;

import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.PostRepository;
import com.example.revHubBack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

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

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        testPost = new Post();
        testPost.setId(1L);
        testPost.setContent("Test post content");
        testPost.setAuthor(testUser);
    }

    @Test
    void getUniversalPosts_Success() {
        List<Post> posts = Arrays.asList(testPost);
        Page<Post> postPage = new PageImpl<>(posts);
        
        when(postRepository.findPublicPosts(any(Pageable.class))).thenReturn(postPage);

        Page<Post> result = postService.getUniversalPosts(mock(Pageable.class));

        assertEquals(1, result.getContent().size());
        assertEquals(testPost, result.getContent().get(0));
    }

    @Test
    void createPost_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        Post result = postService.createPost("Test content", null, "testuser", "PUBLIC");

        assertNotNull(result);
        assertEquals("Test post content", result.getContent());
        assertEquals(testUser, result.getAuthor());
    }

    @Test
    void createPost_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            postService.createPost("Test content", null, "nonexistent", "PUBLIC"));
    }

    @Test
    void deletePost_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        assertDoesNotThrow(() -> postService.deletePost(1L, "testuser"));
        verify(postRepository).delete(testPost);
    }

    @Test
    void deletePost_Unauthorized() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        assertThrows(RuntimeException.class, () -> 
            postService.deletePost(1L, "otheruser"));
    }
}