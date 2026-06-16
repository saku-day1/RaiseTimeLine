package com.example.raisetimeline.service;

import com.example.raisetimeline.dto.request.CreatePostRequest;
import com.example.raisetimeline.dto.response.PostSummaryDto;
import com.example.raisetimeline.entity.Post;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.exception.ForbiddenException;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.repository.LikeRepository;
import com.example.raisetimeline.repository.PostRepository;
import com.example.raisetimeline.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepository postRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    LikeRepository likeRepository;
    @Mock
    S3Service s3Service;

    @InjectMocks
    PostService postService;

    @BeforeEach
    void stubS3() {
        lenient().when(s3Service.resolveUrl(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(s3Service.resolveUrl(null)).thenReturn(null);
    }

    // --- getAll ---

    @Test
    void getAll_anonymousUser_doesNotSetLikedFlag() {
        PostSummaryDto dto = buildDto(1L, 1L);
        Page<PostSummaryDto> page = new PageImpl<>(List.of(dto));
        when(postRepository.findAllWithCounts(any())).thenReturn(page);

        Page<PostSummaryDto> result = postService.getAll(0, 20, null);

        assertThat(result.getContent().get(0).isLiked()).isFalse();
        verify(likeRepository, never()).findLikedPostIdsByUserIdAndPostIds(any(), any());
    }

    @Test
    void getAll_authenticatedUser_setsLikedFlag() {
        PostSummaryDto dto = buildDto(1L, 1L);
        Page<PostSummaryDto> page = new PageImpl<>(List.of(dto));
        when(postRepository.findAllWithCounts(any())).thenReturn(page);
        when(likeRepository.findLikedPostIdsByUserIdAndPostIds(eq(99L), anyList())).thenReturn(Set.of(1L));

        Page<PostSummaryDto> result = postService.getAll(0, 20, 99L);

        assertThat(result.getContent().get(0).isLiked()).isTrue();
    }

    @Test
    void getAll_emptyPage_skipsLikeResolution() {
        when(postRepository.findAllWithCounts(any())).thenReturn(Page.empty());

        postService.getAll(0, 20, 1L);

        verify(likeRepository, never()).findLikedPostIdsByUserIdAndPostIds(any(), any());
    }

    // --- getFollowingPosts ---

    @Test
    void getFollowingPosts_authenticatedUser_setsLikedFlag() {
        PostSummaryDto dto = buildDto(1L, 2L);
        Page<PostSummaryDto> page = new PageImpl<>(List.of(dto));
        when(postRepository.findFollowingUsersPosts(eq(10L), any())).thenReturn(page);
        when(likeRepository.findLikedPostIdsByUserIdAndPostIds(eq(10L), anyList())).thenReturn(Set.of(1L));

        Page<PostSummaryDto> result = postService.getFollowingPosts(0, 20, 10L);

        assertThat(result.getContent().get(0).isLiked()).isTrue();
    }

    @Test
    void getFollowingPosts_emptyPage_returnsEmpty() {
        when(postRepository.findFollowingUsersPosts(any(), any())).thenReturn(Page.empty());

        Page<PostSummaryDto> result = postService.getFollowingPosts(0, 20, 1L);

        assertThat(result.getContent()).isEmpty();
    }

    // --- getById ---

    @Test
    void getById_existingPost_returnsDto() {
        PostSummaryDto dto = buildDto(5L, 1L);
        when(postRepository.findByIdWithCounts(5L)).thenReturn(dto);

        PostSummaryDto result = postService.getById(5L, null);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void getById_nonExistentPost_throwsResourceNotFoundException() {
        when(postRepository.findByIdWithCounts(99L)).thenReturn(null);

        assertThatThrownBy(() -> postService.getById(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_authenticatedUser_setsLikedFlag() {
        PostSummaryDto dto = buildDto(5L, 1L);
        when(postRepository.findByIdWithCounts(5L)).thenReturn(dto);
        when(likeRepository.existsByPostIdAndUserId(5L, 2L)).thenReturn(true);

        PostSummaryDto result = postService.getById(5L, 2L);

        assertThat(result.isLiked()).isTrue();
    }

    @Test
    void getById_anonymousUser_likedIsFalse() {
        PostSummaryDto dto = buildDto(5L, 1L);
        when(postRepository.findByIdWithCounts(5L)).thenReturn(dto);

        PostSummaryDto result = postService.getById(5L, null);

        assertThat(result.isLiked()).isFalse();
        verify(likeRepository, never()).existsByPostIdAndUserId(any(), any());
    }

    // --- create ---

    @Test
    void create_withContent_savesPost() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        PostSummaryDto dto = buildDto(10L, 1L);
        when(postRepository.findByIdWithCounts(any())).thenReturn(dto);
        when(postRepository.save(any())).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PostSummaryDto result = postService.create(buildCreateRequest("hello", null), 1L);

        assertThat(result).isNotNull();
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void create_withImageUrl_savesPost() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        PostSummaryDto dto = buildDto(10L, 1L);
        when(postRepository.findByIdWithCounts(any())).thenReturn(dto);
        when(postRepository.save(any())).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        postService.create(buildCreateRequest(null, "https://s3.example.com/img.jpg"), 1L);

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void create_noContentNoImage_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> postService.create(buildCreateRequest(null, null), 1L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(postRepository, never()).save(any());
    }

    @Test
    void create_blankContent_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> postService.create(buildCreateRequest("   ", null), 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_unknownUser_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.create(buildCreateRequest("text", null), 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- delete ---

    @Test
    void delete_ownPost_deletesSuccessfully() {
        Post post = createPost(1L, 5L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.delete(1L, 5L);

        verify(postRepository).delete(post);
    }

    @Test
    void delete_notOwner_throwsForbiddenException() {
        Post post = createPost(1L, 5L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.delete(1L, 99L))
                .isInstanceOf(ForbiddenException.class);
        verify(postRepository, never()).delete(any());
    }

    @Test
    void delete_nonExistentPost_throwsResourceNotFoundException() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.delete(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- helpers ---

    private PostSummaryDto buildDto(Long postId, Long userId) {
        return new PostSummaryDto(postId, "content", null, LocalDateTime.now(),
                userId, "user", null, 0L, 0L);
    }

    private CreatePostRequest buildCreateRequest(String content, String imageUrl) {
        CreatePostRequest req = new CreatePostRequest();
        req.setContent(content);
        req.setImageUrl(imageUrl);
        return req;
    }

    private User createUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user");
        u.setDisplayName("User");
        return u;
    }

    private Post createPost(Long postId, Long ownerId) {
        User owner = createUser(ownerId);
        Post p = new Post();
        p.setId(postId);
        p.setUser(owner);
        p.setContent("content");
        return p;
    }
}
