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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getAll(int page, int size, Long currentUserId) {
        Page<PostSummaryDto> posts = postRepository.findAllWithCounts(PageRequest.of(page, size));

        if (!posts.isEmpty()) {
            resolveImageUrls(posts.getContent());
            if (currentUserId != null) {
                List<Long> postIds = posts.getContent().stream().map(PostSummaryDto::getId).toList();
                Set<Long> likedIds = likeRepository.findLikedPostIdsByUserIdAndPostIds(currentUserId, postIds);
                posts.getContent().forEach(p -> p.setLiked(likedIds.contains(p.getId())));
            }
        }

        return posts;
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getFollowingPosts(int page, int size, Long currentUserId) {
        Page<PostSummaryDto> posts = postRepository.findFollowingUsersPosts(currentUserId, PageRequest.of(page, size));
        if (!posts.isEmpty()) {
            resolveImageUrls(posts.getContent());
            List<Long> postIds = posts.getContent().stream().map(PostSummaryDto::getId).toList();
            Set<Long> likedIds = likeRepository.findLikedPostIdsByUserIdAndPostIds(currentUserId, postIds);
            posts.getContent().forEach(p -> p.setLiked(likedIds.contains(p.getId())));
        }
        return posts;
    }

    @Transactional(readOnly = true)
    public PostSummaryDto getById(Long postId, Long currentUserId) {
        PostSummaryDto post = postRepository.findByIdWithCounts(postId);
        if (post == null) {
            throw new ResourceNotFoundException("投稿が見つかりません");
        }
        resolveImageUrls(List.of(post));
        if (currentUserId != null) {
            boolean liked = likeRepository.existsByPostIdAndUserId(postId, currentUserId);
            post.setLiked(liked);
        }
        return post;
    }

    @Transactional
    public PostSummaryDto create(CreatePostRequest req, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));

        Post post = new Post();
        post.setUser(user);
        post.setContent(req.getContent());
        post.setImageUrl(req.getImageUrl());
        postRepository.save(post);

        PostSummaryDto dto = postRepository.findByIdWithCounts(post.getId());
        resolveImageUrls(List.of(dto));
        return dto;
    }

    public String uploadPostImage(Long userId, MultipartFile file) throws IOException {
        String key = s3Service.uploadImage("posts", userId, file);
        return s3Service.generatePresignedUrl(key);
    }

    @Transactional
    public void delete(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("投稿が見つかりません"));

        if (!post.getUser().getId().equals(userId)) {
            throw new ForbiddenException("自分の投稿のみ削除できます");
        }

        postRepository.delete(post);
    }

    private void resolveImageUrls(List<PostSummaryDto> posts) {
        posts.forEach(p -> {
            p.setImageUrl(s3Service.resolveUrl(p.getImageUrl()));
            p.setProfileImageUrl(s3Service.resolveUrl(p.getProfileImageUrl()));
        });
    }
}
