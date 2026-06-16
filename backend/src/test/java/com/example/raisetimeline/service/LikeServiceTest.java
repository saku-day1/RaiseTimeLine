package com.example.raisetimeline.service;

import com.example.raisetimeline.dto.response.LikeStatusResponse;
import com.example.raisetimeline.entity.Like;
import com.example.raisetimeline.entity.Post;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.repository.LikeRepository;
import com.example.raisetimeline.repository.PostRepository;
import com.example.raisetimeline.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    LikeRepository likeRepository;
    @Mock
    PostRepository postRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    LikeService likeService;

    // --- like ---

    @Test
    void like_notYetLiked_savesLikeAndReturnsLikedTrue() {
        Post post = buildPost(1L);
        User user = buildUser(2L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(likeRepository.countByPostId(1L)).thenReturn(1L);

        LikeStatusResponse res = likeService.like(1L, 2L);

        assertThat(res.isLiked()).isTrue();
        assertThat(res.getCount()).isEqualTo(1L);
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    void like_alreadyLiked_idempotentReturnsLikedTrueWithoutDuplicate() {
        Post post = buildPost(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(true);
        when(likeRepository.countByPostId(1L)).thenReturn(1L);

        LikeStatusResponse res = likeService.like(1L, 2L);

        assertThat(res.isLiked()).isTrue();
        verify(likeRepository, never()).save(any());
    }

    @Test
    void like_nonExistentPost_throwsResourceNotFoundException() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.like(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void like_nonExistentUser_throwsResourceNotFoundException() {
        Post post = buildPost(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByPostIdAndUserId(1L, 99L)).thenReturn(false);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.like(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- unlike ---

    @Test
    void unlike_existingLike_deletesAndReturnsFalse() {
        Like like = new Like();
        when(postRepository.existsById(1L)).thenReturn(true);
        when(likeRepository.findByPostIdAndUserId(1L, 2L)).thenReturn(Optional.of(like));
        when(likeRepository.countByPostId(1L)).thenReturn(0L);

        LikeStatusResponse res = likeService.unlike(1L, 2L);

        assertThat(res.isLiked()).isFalse();
        assertThat(res.getCount()).isEqualTo(0L);
        verify(likeRepository).delete(like);
    }

    @Test
    void unlike_noLikeExists_idempotentReturnsFalse() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(likeRepository.findByPostIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        when(likeRepository.countByPostId(1L)).thenReturn(0L);

        LikeStatusResponse res = likeService.unlike(1L, 2L);

        assertThat(res.isLiked()).isFalse();
        verify(likeRepository, never()).delete(any());
    }

    @Test
    void unlike_nonExistentPost_throwsResourceNotFoundException() {
        when(postRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> likeService.unlike(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getStatus ---

    @Test
    void getStatus_liked_returnsCorrectStatus() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(true);
        when(likeRepository.countByPostId(1L)).thenReturn(3L);

        LikeStatusResponse res = likeService.getStatus(1L, 2L);

        assertThat(res.isLiked()).isTrue();
        assertThat(res.getCount()).isEqualTo(3L);
    }

    @Test
    void getStatus_notLiked_returnsCorrectStatus() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(false);
        when(likeRepository.countByPostId(1L)).thenReturn(0L);

        LikeStatusResponse res = likeService.getStatus(1L, 2L);

        assertThat(res.isLiked()).isFalse();
    }

    @Test
    void getStatus_nonExistentPost_throwsResourceNotFoundException() {
        when(postRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> likeService.getStatus(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- helpers ---

    private Post buildPost(Long id) {
        Post p = new Post();
        p.setId(id);
        p.setContent("content");
        return p;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user" + id);
        u.setDisplayName("User");
        return u;
    }
}
