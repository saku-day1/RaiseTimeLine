package com.example.raisetimeline.service;

import com.example.raisetimeline.dto.response.FollowStatusResponse;
import com.example.raisetimeline.dto.response.UserProfileDto;
import com.example.raisetimeline.dto.response.UserSummaryDto;
import com.example.raisetimeline.entity.Follow;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.exception.ForbiddenException;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.repository.FollowRepository;
import com.example.raisetimeline.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    FollowRepository followRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    S3Service s3Service;

    @InjectMocks
    FollowService followService;

    @BeforeEach
    void stubS3() {
        lenient().when(s3Service.resolveUrl(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(s3Service.resolveUrl(null)).thenReturn(null);
    }

    // --- follow ---

    @Test
    void follow_newFollow_savesAndReturnsStatus() {
        User target = buildUser(2L);
        User current = buildUser(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(current));
        when(followRepository.countByFollowingId(2L)).thenReturn(1L);
        when(followRepository.countByFollowerId(2L)).thenReturn(0L);

        FollowStatusResponse res = followService.follow(2L, 1L);

        assertThat(res.following()).isTrue();
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void follow_alreadyFollowing_idempotentNoExtraInsert() {
        User target = buildUser(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);
        when(followRepository.countByFollowingId(2L)).thenReturn(1L);
        when(followRepository.countByFollowerId(2L)).thenReturn(0L);

        followService.follow(2L, 1L);

        verify(followRepository, never()).save(any());
    }

    @Test
    void follow_selfFollow_throwsForbiddenException() {
        assertThatThrownBy(() -> followService.follow(1L, 1L))
                .isInstanceOf(ForbiddenException.class);
        verify(followRepository, never()).save(any());
    }

    @Test
    void follow_targetNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.follow(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- unfollow ---

    @Test
    void unfollow_existingFollow_deletesAndReturnsStatus() {
        Follow follow = new Follow();
        when(userRepository.existsById(2L)).thenReturn(true);
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.of(follow));
        when(followRepository.countByFollowingId(2L)).thenReturn(0L);
        when(followRepository.countByFollowerId(2L)).thenReturn(0L);

        FollowStatusResponse res = followService.unfollow(2L, 1L);

        assertThat(res.following()).isFalse();
        verify(followRepository).delete(follow);
    }

    @Test
    void unfollow_noFollowExists_idempotentNoError() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.empty());
        when(followRepository.countByFollowingId(2L)).thenReturn(0L);
        when(followRepository.countByFollowerId(2L)).thenReturn(0L);

        followService.unfollow(2L, 1L);

        verify(followRepository, never()).delete(any());
    }

    @Test
    void unfollow_targetNotFound_throwsResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> followService.unfollow(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getStatus ---

    @Test
    void getStatus_targetExists_returnsFollowStatus() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);
        when(followRepository.countByFollowingId(2L)).thenReturn(5L);
        when(followRepository.countByFollowerId(2L)).thenReturn(3L);

        FollowStatusResponse res = followService.getStatus(2L, 1L);

        assertThat(res.following()).isTrue();
        assertThat(res.followerCount()).isEqualTo(5L);
        assertThat(res.followingCount()).isEqualTo(3L);
    }

    @Test
    void getStatus_targetNotFound_throwsResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> followService.getStatus(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- searchUsers ---

    @Test
    void searchUsers_returnsUsersWithFollowingFlag() {
        User u = buildUser(3L);
        when(userRepository.searchByUsername("alice", 1L)).thenReturn(List.of(u));
        when(followRepository.findFollowingIdsByFollowerId(1L)).thenReturn(List.of(3L));

        List<UserSummaryDto> result = followService.searchUsers("alice", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).following()).isTrue();
    }

    @Test
    void searchUsers_emptyResult_returnsEmptyList() {
        when(userRepository.searchByUsername("zzz", 1L)).thenReturn(List.of());
        when(followRepository.findFollowingIdsByFollowerId(1L)).thenReturn(List.of());

        List<UserSummaryDto> result = followService.searchUsers("zzz", 1L);

        assertThat(result).isEmpty();
    }

    // --- getUserProfile ---

    @Test
    void getUserProfile_returnsProfileWithCounts() {
        User target = buildUser(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);
        when(followRepository.existsByFollowerIdAndFollowingId(2L, 1L)).thenReturn(false);
        when(followRepository.countByFollowingId(2L)).thenReturn(10L);
        when(followRepository.countByFollowerId(2L)).thenReturn(5L);

        UserProfileDto dto = followService.getUserProfile(2L, 1L);

        assertThat(dto.followerCount()).isEqualTo(10L);
        assertThat(dto.followingCount()).isEqualTo(5L);
        assertThat(dto.following()).isFalse();
    }

    @Test
    void getUserProfile_anonymousViewer_followingAndFollowedBackAreFalse() {
        User target = buildUser(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(followRepository.countByFollowingId(anyLong())).thenReturn(0L);
        when(followRepository.countByFollowerId(anyLong())).thenReturn(0L);

        UserProfileDto dto = followService.getUserProfile(2L, null);

        assertThat(dto.following()).isFalse();
        assertThat(dto.followedBack()).isFalse();
    }

    @Test
    void getUserProfile_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.getUserProfile(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getFollowers ---

    @Test
    void getFollowers_returnsFollowerList() {
        User follower = buildUser(3L);
        when(userRepository.existsById(2L)).thenReturn(true);
        when(followRepository.findFollowersByFollowingId(2L)).thenReturn(List.of(follower));
        when(followRepository.findFollowingIdsByFollowerId(1L)).thenReturn(List.of());

        List<UserSummaryDto> result = followService.getFollowers(2L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(3L);
    }

    @Test
    void getFollowers_targetNotFound_throwsResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> followService.getFollowers(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getFollowing ---

    @Test
    void getFollowing_returnsFollowingList() {
        User following = buildUser(4L);
        when(userRepository.existsById(2L)).thenReturn(true);
        when(followRepository.findFollowingsByFollowerId(2L)).thenReturn(List.of(following));
        when(followRepository.findFollowingIdsByFollowerId(1L)).thenReturn(List.of());

        List<UserSummaryDto> result = followService.getFollowing(2L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(4L);
    }

    @Test
    void getFollowing_targetNotFound_throwsResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> followService.getFollowing(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- helpers ---

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user" + id);
        u.setDisplayName("User " + id);
        return u;
    }
}
