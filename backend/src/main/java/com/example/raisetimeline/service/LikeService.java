package com.example.raisetimeline.service;

import com.example.raisetimeline.dto.response.LikeStatusResponse;
import com.example.raisetimeline.entity.Like;
import com.example.raisetimeline.entity.Post;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.repository.LikeRepository;
import com.example.raisetimeline.repository.PostRepository;
import com.example.raisetimeline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeStatusResponse like(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("投稿が見つかりません"));

        // べき等設計: すでにいいね済みの場合は現在の状態をそのまま返す
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return new LikeStatusResponse(true, likeRepository.countByPostId(postId));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));

        Like like = new Like();
        like.setPost(post);
        like.setUser(user);
        likeRepository.save(like);

        return new LikeStatusResponse(true, likeRepository.countByPostId(postId));
    }

    @Transactional
    public LikeStatusResponse unlike(Long postId, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("投稿が見つかりません");
        }

        // べき等設計: いいねがなければそのまま返す
        likeRepository.findByPostIdAndUserId(postId, userId)
                .ifPresent(likeRepository::delete);

        return new LikeStatusResponse(false, likeRepository.countByPostId(postId));
    }

    @Transactional(readOnly = true)
    public LikeStatusResponse getStatus(Long postId, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("投稿が見つかりません");
        }
        boolean liked = likeRepository.existsByPostIdAndUserId(postId, userId);
        long count = likeRepository.countByPostId(postId);
        return new LikeStatusResponse(liked, count);
    }
}
