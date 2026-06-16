package com.example.raisetimeline.service;

import com.example.raisetimeline.dto.request.CreateCommentRequest;
import com.example.raisetimeline.dto.response.CommentResponse;
import com.example.raisetimeline.entity.Comment;
import com.example.raisetimeline.entity.Post;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.exception.ForbiddenException;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.repository.CommentRepository;
import com.example.raisetimeline.repository.PostRepository;
import com.example.raisetimeline.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;
    @Mock
    PostRepository postRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    CommentService commentService;

    // --- getByPostId ---

    @Test
    void getByPostId_existingPost_returnsCommentList() {
        when(postRepository.existsById(1L)).thenReturn(true);
        Comment comment = buildComment(10L, buildUser(2L), buildPost(1L));
        when(commentRepository.findByPostIdWithUser(1L)).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.getByPostId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
    }

    @Test
    void getByPostId_nonExistentPost_throwsResourceNotFoundException() {
        when(postRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> commentService.getByPostId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- create ---

    @Test
    void create_validInput_savesAndReturnsCommentResponse() {
        Post post = buildPost(1L);
        User user = buildUser(2L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(commentRepository.save(any())).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        CommentResponse res = commentService.create(1L, buildRequest("hello"), 2L);

        assertThat(res.getId()).isEqualTo(10L);
        assertThat(res.getContent()).isEqualTo("hello");
    }

    @Test
    void create_nonExistentPost_throwsResourceNotFoundException() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(99L, buildRequest("text"), 1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_nonExistentUser_throwsResourceNotFoundException() {
        Post post = buildPost(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(1L, buildRequest("text"), 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- delete ---

    @Test
    void delete_ownComment_deletesSuccessfully() {
        User user = buildUser(1L);
        Comment comment = buildComment(5L, user, buildPost(1L));
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        commentService.delete(5L, 1L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_notOwner_throwsForbiddenException() {
        User owner = buildUser(1L);
        Comment comment = buildComment(5L, owner, buildPost(1L));
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(5L, 99L))
                .isInstanceOf(ForbiddenException.class);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void delete_nonExistentComment_throwsResourceNotFoundException() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- helpers ---

    private CreateCommentRequest buildRequest(String content) {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent(content);
        return req;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user" + id);
        u.setDisplayName("User " + id);
        return u;
    }

    private Post buildPost(Long id) {
        Post p = new Post();
        p.setId(id);
        p.setContent("post content");
        return p;
    }

    private Comment buildComment(Long id, User user, Post post) {
        Comment c = new Comment();
        c.setId(id);
        c.setUser(user);
        c.setPost(post);
        c.setContent("comment");
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }
}
