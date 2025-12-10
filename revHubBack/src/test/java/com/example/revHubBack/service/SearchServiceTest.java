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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private SearchService searchService;

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
    void searchUsers_Success() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByUsernameContainingIgnoreCase("test")).thenReturn(users);

        List<User> result = searchService.searchUsers("test");

        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
    }

    @Test
    void searchUsers_EmptyQuery() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = searchService.searchUsers("");

        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void search_Success() {
        List<User> users = Arrays.asList(testUser);
        List<Post> posts = Arrays.asList(testPost);
        when(userRepository.findByUsernameContainingIgnoreCase("test")).thenReturn(users);
        when(postRepository.findAllByOrderByCreatedDateDesc()).thenReturn(posts);

        java.util.Map<String, Object> result = searchService.search("test");

        assertNotNull(result.get("users"));
        assertNotNull(result.get("posts"));
    }

    @Test
    void search_NullQuery() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        java.util.Map<String, Object> result = searchService.search(null);

        assertNotNull(result.get("users"));
        verify(userRepository).findAll();
    }
}