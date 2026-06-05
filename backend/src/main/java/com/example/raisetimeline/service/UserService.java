package com.example.raisetimeline.service;

import com.example.raisetimeline.dto.request.UpdateProfileRequest;
import com.example.raisetimeline.dto.response.UserProfileDto;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.repository.FollowRepository;
import com.example.raisetimeline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final S3Service s3Service;

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
        user.setDisplayName(req.getDisplayName());
        user.setBio(req.getBio());
        userRepository.save(user);
        return toProfileDto(user, userId);
    }

    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        String key = s3Service.uploadImage("profiles", userId, file);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
        user.setProfileImageUrl(key);
        userRepository.save(user);
        return s3Service.generatePresignedUrl(key);
    }

    @Transactional
    public String uploadHeaderImage(Long userId, MultipartFile file) throws IOException {
        String key = s3Service.uploadImage("headers", userId, file);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
        user.setHeaderImageUrl(key);
        userRepository.save(user);
        return s3Service.generatePresignedUrl(key);
    }

    private UserProfileDto toProfileDto(User user, Long currentUserId) {
        boolean following = followRepository.existsByFollowerIdAndFollowingId(currentUserId, user.getId());
        boolean followedBack = followRepository.existsByFollowerIdAndFollowingId(user.getId(), currentUserId);
        long followerCount = followRepository.countByFollowingId(user.getId());
        long followingCount = followRepository.countByFollowerId(user.getId());
        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                s3Service.resolveUrl(user.getProfileImageUrl()),
                s3Service.resolveUrl(user.getHeaderImageUrl()),
                user.getBio(),
                followerCount,
                followingCount,
                following,
                followedBack
        );
    }
}
