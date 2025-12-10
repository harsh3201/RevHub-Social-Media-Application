package com.example.revHubBack.service;

import com.example.revHubBack.entity.Like;
import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.LikeRepository;
import com.example.revHubBack.repository.PostRepository;
import com.example.revHubBack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeService likeService;

    private User testUser;
    private Post testPost;
    private Like testLike;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        testPost = new Post();
        testPost.setId(1L);
        testPost.setLikesCount(0);
        
        testLike = new Like();
        testLike.setUser(testUser);
        testLike.setPost(testPost);
    }

    @Test
    void toggleLike_LikePost() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(likeRepository.existsByUserAndPost(testUser, testPost)).thenReturn(false);
        when(likeRepository.save(any(Like.class))).thenReturn(testLike);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        java.util.Map<String, Object> result = likeService.toggleLike(1L, "testuser");

        assertTrue((Boolean) result.get("isLiked"));
        assertEquals(1, result.get("likesCount"));
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    void toggleLike_UnlikePost() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(likeRepository.existsByUserAndPost(testUser, testPost)).thenReturn(true);
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(testLike));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        java.util.Map<String, Object> result = likeService.toggleLike(1L, "testuser");

        assertFalse((Boolean) result.get("isLiked"));
        verify(likeRepository).delete(testLike);
    }

    @Test
    void toggleLike_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            likeService.toggleLike(1L, "nonexistent"));
    }

    @Test
    void toggleLike_PostNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            likeService.toggleLike(999L, "testuser"));
    }
}