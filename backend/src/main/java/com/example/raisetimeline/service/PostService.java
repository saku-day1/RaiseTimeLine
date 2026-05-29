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

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getAll(int page, int size, Long currentUserId) {
        Page<PostSummaryDto> posts = postRepository.findAllWithCounts(PageRequest.of(page, size));

        if (currentUserId != null && !posts.isEmpty()) {
            // N+1防止: IN句で一括取得
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

        return postRepository.findByIdWithCounts(post.getId());
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
}
