package com.example.raisetimeline.service;

import com.example.raisetimeline.dto.request.UpdateProfileRequest;
import com.example.raisetimeline.dto.response.UserProfileDto;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.repository.FollowRepository;
import com.example.raisetimeline.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    FollowRepository followRepository;
    @Mock
    S3Service s3Service;

    @InjectMocks
    UserService userService;

    @BeforeEach
    void stubS3() {
        lenient().when(s3Service.resolveUrl(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(s3Service.resolveUrl(null)).thenReturn(null);
    }

    // --- updateProfile ---

    @Test
    void updateProfile_userExists_updatesAndReturnsDto() {
        User user = buildUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(followRepository.existsByFollowerIdAndFollowingId(any(), any())).thenReturn(false);
        when(followRepository.countByFollowingId(1L)).thenReturn(0L);
        when(followRepository.countByFollowerId(1L)).thenReturn(0L);

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setDisplayName("New Name");
        req.setBio("My bio");

        UserProfileDto dto = userService.updateProfile(1L, req);

        assertThat(dto.displayName()).isEqualTo("New Name");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setDisplayName("Name");

        assertThatThrownBy(() -> userService.updateProfile(99L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- uploadProfileImage ---

    @Test
    void uploadProfileImage_userExists_savesKeyAndReturnsPresignedUrl() throws Exception {
        User user = buildUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(s3Service.uploadImage(eq("profiles"), eq(1L), any())).thenReturn("profiles/1/uuid.jpg");
        when(s3Service.generatePresignedUrl("profiles/1/uuid.jpg")).thenReturn("https://s3.example.com/presigned");

        String url = userService.uploadProfileImage(1L, mockFile());

        assertThat(url).isEqualTo("https://s3.example.com/presigned");
        assertThat(user.getProfileImageUrl()).isEqualTo("profiles/1/uuid.jpg");
    }

    @Test
    void uploadProfileImage_userNotFound_throwsResourceNotFoundException() throws Exception {
        when(s3Service.uploadImage(any(), any(), any())).thenReturn("profiles/99/uuid.jpg");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.uploadProfileImage(99L, mockFile()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- uploadHeaderImage ---

    @Test
    void uploadHeaderImage_userExists_savesKeyAndReturnsPresignedUrl() throws Exception {
        User user = buildUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(s3Service.uploadImage(eq("headers"), eq(1L), any())).thenReturn("headers/1/uuid.jpg");
        when(s3Service.generatePresignedUrl("headers/1/uuid.jpg")).thenReturn("https://s3.example.com/presigned");

        String url = userService.uploadHeaderImage(1L, mockFile());

        assertThat(url).isEqualTo("https://s3.example.com/presigned");
        assertThat(user.getHeaderImageUrl()).isEqualTo("headers/1/uuid.jpg");
    }

    @Test
    void uploadHeaderImage_userNotFound_throwsResourceNotFoundException() throws Exception {
        when(s3Service.uploadImage(any(), any(), any())).thenReturn("headers/99/uuid.jpg");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.uploadHeaderImage(99L, mockFile()))
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

    private MockMultipartFile mockFile() {
        return new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }
}
