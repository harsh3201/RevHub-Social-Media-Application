package com.example.revHubBack.service;

import com.example.revHubBack.entity.Comment;
import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.CommentRepository;
import com.example.revHubBack.repository.PostRepository;
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

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        testPost = new Post();
        testPost.setId(1L);
        testPost.setCommentsCount(0);
        
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("Test comment");
        testComment.setAuthor(testUser);
        testComment.setPost(testPost);
    }

    @Test
    void addComment_Success() {
        com.example.revHubBack.dto.CommentRequest request = new com.example.revHubBack.dto.CommentRequest();
        request.setContent("Test comment");
        
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        Comment result = commentService.addComment(1L, request, "testuser");

        assertNotNull(result);
        verify(commentRepository).save(any(Comment.class));
        verify(postRepository).save(testPost);
    }

    @Test
    void getCommentsByPost_Success() {
        List<Comment> comments = Arrays.asList(testComment);
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.findByPostAndParentCommentIsNullOrderByCreatedDateDesc(testPost)).thenReturn(comments);

        List<Comment> result = commentService.getCommentsByPost(1L);

        assertEquals(1, result.size());
        assertEquals(testComment, result.get(0));
    }

    @Test
    void deleteComment_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        assertDoesNotThrow(() -> commentService.deleteComment(1L, 1L, "testuser"));
        verify(commentRepository).delete(testComment);
    }

    @Test
    void addComment_PostNotFound() {
        com.example.revHubBack.dto.CommentRequest request = new com.example.revHubBack.dto.CommentRequest();
        request.setContent("Test comment");
        
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            commentService.addComment(999L, request, "testuser"));
    }
}