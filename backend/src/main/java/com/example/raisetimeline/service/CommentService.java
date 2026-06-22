package com.example.raisetimeline.service;

import com.example.raisetimeline.dto.request.CreateCommentRequest;
import com.example.raisetimeline.dto.response.CommentResponse;
import com.example.raisetimeline.entity.Comment;
import com.example.raisetimeline.entity.Post;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.exception.ForbiddenException;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.repository.CommentRepository;
import com.example.raisetimeline.repository.PostRepository;
import com.example.raisetimeline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentResponse> getByPostId(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("投稿が見つかりません");
        }
        // N+1防止: JOIN FETCH で1クエリ取得
        return commentRepository.findByPostIdWithUser(postId)
                .stream()
                .map(CommentResponse::new)
                .toList();
    }

    @Transactional
    public CommentResponse create(Long postId, CreateCommentRequest req, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("投稿が見つかりません"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(req.getContent());
        commentRepository.save(comment);
        log.info("event=comment.create.success userId={} postId={} commentId={}", userId, postId, comment.getId());

        return new CommentResponse(comment);
    }

    @Transactional
    public void delete(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("コメントが見つかりません"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("自分のコメントのみ削除できます");
        }

        commentRepository.delete(comment);
        log.info("event=comment.delete.success userId={} commentId={}", userId, commentId);
    }
}
