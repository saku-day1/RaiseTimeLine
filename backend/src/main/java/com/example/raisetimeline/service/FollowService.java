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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public FollowStatusResponse follow(Long targetUserId, Long currentUserId) {
        if (targetUserId.equals(currentUserId)) {
            throw new ForbiddenException("自分自身をフォローすることはできません");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));

        if (!followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)) {
            User current = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
            Follow follow = new Follow();
            follow.setFollower(current);
            follow.setFollowing(target);
            followRepository.save(follow);
        }

        return buildStatus(targetUserId, currentUserId);
    }

    @Transactional
    public FollowStatusResponse unfollow(Long targetUserId, Long currentUserId) {
        if (!userRepository.existsById(targetUserId)) {
            throw new ResourceNotFoundException("ユーザーが見つかりません");
        }

        followRepository.findByFollowerIdAndFollowingId(currentUserId, targetUserId)
                .ifPresent(followRepository::delete);

        return buildStatus(targetUserId, currentUserId);
    }

    @Transactional(readOnly = true)
    public FollowStatusResponse getStatus(Long targetUserId, Long currentUserId) {
        if (!userRepository.existsById(targetUserId)) {
            throw new ResourceNotFoundException("ユーザーが見つかりません");
        }
        return buildStatus(targetUserId, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDto> searchUsers(String keyword, Long currentUserId) {
        List<User> users = userRepository.searchByUsername(keyword, currentUserId);
        Set<Long> followingIds = Set.copyOf(followRepository.findFollowingIdsByFollowerId(currentUserId));

        return users.stream()
                .map(u -> new UserSummaryDto(
                        u.getId(),
                        u.getUsername(),
                        u.getDisplayName(),
                        u.getProfileImageUrl(),
                        followingIds.contains(u.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long targetUserId, Long currentUserId) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
        boolean following = currentUserId != null
                && followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId);
        long followerCount = followRepository.countByFollowingId(targetUserId);
        long followingCount = followRepository.countByFollowerId(targetUserId);
        return new UserProfileDto(
                target.getId(),
                target.getUsername(),
                target.getDisplayName(),
                target.getProfileImageUrl(),
                target.getBio(),
                followerCount,
                followingCount,
                following
        );
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDto> getFollowers(Long targetUserId, Long currentUserId) {
        if (!userRepository.existsById(targetUserId)) {
            throw new ResourceNotFoundException("ユーザーが見つかりません");
        }
        List<User> followers = followRepository.findFollowersByFollowingId(targetUserId);
        Set<Long> followingIds = currentUserId != null
                ? Set.copyOf(followRepository.findFollowingIdsByFollowerId(currentUserId))
                : Set.of();
        return followers.stream()
                .map(u -> new UserSummaryDto(u.getId(), u.getUsername(), u.getDisplayName(), u.getProfileImageUrl(), followingIds.contains(u.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDto> getFollowing(Long targetUserId, Long currentUserId) {
        if (!userRepository.existsById(targetUserId)) {
            throw new ResourceNotFoundException("ユーザーが見つかりません");
        }
        List<User> followings = followRepository.findFollowingsByFollowerId(targetUserId);
        Set<Long> followingIds = currentUserId != null
                ? Set.copyOf(followRepository.findFollowingIdsByFollowerId(currentUserId))
                : Set.of();
        return followings.stream()
                .map(u -> new UserSummaryDto(u.getId(), u.getUsername(), u.getDisplayName(), u.getProfileImageUrl(), followingIds.contains(u.getId())))
                .toList();
    }

    private FollowStatusResponse buildStatus(Long targetUserId, Long currentUserId) {
        boolean following = followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId);
        long followerCount = followRepository.countByFollowingId(targetUserId);
        long followingCount = followRepository.countByFollowerId(targetUserId);
        return new FollowStatusResponse(following, followerCount, followingCount);
    }
}
