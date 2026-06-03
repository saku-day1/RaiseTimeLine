package com.example.raisetimeline.service;

import com.example.raisetimeline.dto.request.UpdateProfileRequest;
import com.example.raisetimeline.dto.response.UserProfileDto;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.repository.FollowRepository;
import com.example.raisetimeline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Value("${app.upload.dir:uploads/profile-images}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

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
        String imageUrl = saveImage(file);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
        return imageUrl;
    }

    @Transactional
    public String uploadHeaderImage(Long userId, MultipartFile file) throws IOException {
        String imageUrl = saveImage(file);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
        user.setHeaderImageUrl(imageUrl);
        userRepository.save(user);
        return imageUrl;
    }

    private String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("ファイルが空です");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/gif"))) {
            throw new IllegalArgumentException("JPEG / PNG / GIF のみアップロードできます");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("ファイルサイズは 5MB 以下にしてください");
        }
        String ext = switch (contentType) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            default -> "jpg";
        };
        String filename = UUID.randomUUID() + "." + ext;
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename));
        return baseUrl + "/uploads/profile-images/" + filename;
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
                user.getProfileImageUrl(),
                user.getHeaderImageUrl(),
                user.getBio(),
                followerCount,
                followingCount,
                following,
                followedBack
        );
    }
}
