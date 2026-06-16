package com.example.raisetimeline.repository;

import com.example.raisetimeline.dto.response.PostSummaryDto;
import com.example.raisetimeline.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PostRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    PostRepository postRepository;

    private User author;
    private User viewer;
    private User other;

    @BeforeEach
    void setUp() {
        author = createUser("author@example.com", "author");
        viewer = createUser("viewer@example.com", "viewer");
        other = createUser("other@example.com", "other");
        em.persistAndFlush(author);
        em.persistAndFlush(viewer);
        em.persistAndFlush(other);
    }

    // --- findAllWithCounts ---

    @Test
    void findAllWithCounts_noPosts_returnsEmptyPage() {
        Page<PostSummaryDto> page = postRepository.findAllWithCounts(PageRequest.of(0, 20));
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findAllWithCounts_withPosts_returnsCorrectLikeCount() {
        Post post = persistPost(author, "hello");
        persistLike(post, viewer);

        Page<PostSummaryDto> page = postRepository.findAllWithCounts(PageRequest.of(0, 20));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getLikeCount()).isEqualTo(1L);
    }

    @Test
    void findAllWithCounts_withPosts_returnsCorrectCommentCount() {
        Post post = persistPost(author, "hello");
        persistComment(post, viewer, "nice");
        persistComment(post, other, "great");

        Page<PostSummaryDto> page = postRepository.findAllWithCounts(PageRequest.of(0, 20));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getCommentCount()).isEqualTo(2L);
    }

    @Test
    void findAllWithCounts_pageable_respectsPageSize() {
        persistPost(author, "post1");
        persistPost(author, "post2");
        persistPost(author, "post3");

        Page<PostSummaryDto> page = postRepository.findAllWithCounts(PageRequest.of(0, 2));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3L);
    }

    @Test
    void findAllWithCounts_dtoFieldsMapping_allFieldsPopulated() {
        Post post = persistPost(author, "content here");

        Page<PostSummaryDto> page = postRepository.findAllWithCounts(PageRequest.of(0, 20));
        PostSummaryDto dto = page.getContent().get(0);

        assertThat(dto.getId()).isEqualTo(post.getId());
        assertThat(dto.getContent()).isEqualTo("content here");
        assertThat(dto.getUserId()).isEqualTo(author.getId());
        assertThat(dto.getUsername()).isEqualTo("author");
        assertThat(dto.getCreatedAt()).isNotNull();
    }

    @Test
    void findAllWithCounts_zeroLikesAndComments_returnsZeroCounts() {
        persistPost(author, "lonely post");

        Page<PostSummaryDto> page = postRepository.findAllWithCounts(PageRequest.of(0, 20));
        PostSummaryDto dto = page.getContent().get(0);
        assertThat(dto.getLikeCount()).isEqualTo(0L);
        assertThat(dto.getCommentCount()).isEqualTo(0L);
    }

    // --- findByIdWithCounts ---

    @Test
    void findByIdWithCounts_existingPost_returnsCorrectCounts() {
        Post post = persistPost(author, "test");
        persistLike(post, viewer);
        persistComment(post, viewer, "hello");

        PostSummaryDto dto = postRepository.findByIdWithCounts(post.getId());
        assertThat(dto).isNotNull();
        assertThat(dto.getLikeCount()).isEqualTo(1L);
        assertThat(dto.getCommentCount()).isEqualTo(1L);
    }

    @Test
    void findByIdWithCounts_postWithNoLikesOrComments_returnsZeroCounts() {
        Post post = persistPost(author, "empty");

        PostSummaryDto dto = postRepository.findByIdWithCounts(post.getId());
        assertThat(dto).isNotNull();
        assertThat(dto.getLikeCount()).isEqualTo(0L);
        assertThat(dto.getCommentCount()).isEqualTo(0L);
    }

    @Test
    void findByIdWithCounts_nonExistentPostId_returnsNull() {
        PostSummaryDto dto = postRepository.findByIdWithCounts(99999L);
        assertThat(dto).isNull();
    }

    // --- findFollowingUsersPosts ---

    @Test
    void findFollowingUsersPosts_includesOwnPosts() {
        persistPost(viewer, "my own post");

        Page<PostSummaryDto> page = postRepository.findFollowingUsersPosts(viewer.getId(), PageRequest.of(0, 20));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getUserId()).isEqualTo(viewer.getId());
    }

    @Test
    void findFollowingUsersPosts_includesFollowedUserPosts() {
        persistFollow(viewer, author);
        persistPost(author, "followed author post");

        Page<PostSummaryDto> page = postRepository.findFollowingUsersPosts(viewer.getId(), PageRequest.of(0, 20));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getUserId()).isEqualTo(author.getId());
    }

    @Test
    void findFollowingUsersPosts_excludesUnfollowedUserPosts() {
        persistPost(other, "not following this user");

        Page<PostSummaryDto> page = postRepository.findFollowingUsersPosts(viewer.getId(), PageRequest.of(0, 20));
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findFollowingUsersPosts_includesOwnAndFollowedPosts() {
        persistFollow(viewer, author);
        persistPost(viewer, "my post");
        persistPost(author, "author post");
        persistPost(other, "other post");

        Page<PostSummaryDto> page = postRepository.findFollowingUsersPosts(viewer.getId(), PageRequest.of(0, 20));
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void findFollowingUsersPosts_pageable_respectsPageSize() {
        persistFollow(viewer, author);
        persistPost(viewer, "post1");
        persistPost(viewer, "post2");
        persistPost(author, "post3");

        Page<PostSummaryDto> page = postRepository.findFollowingUsersPosts(viewer.getId(), PageRequest.of(0, 2));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3L);
    }

    // --- helpers ---

    private User createUser(String email, String username) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("hashed");
        u.setUsername(username);
        u.setDisplayName(username);
        return u;
    }

    private Post persistPost(User user, String content) {
        Post p = new Post();
        p.setUser(user);
        p.setContent(content);
        return em.persistAndFlush(p);
    }

    private void persistLike(Post post, User user) {
        Like l = new Like();
        l.setPost(post);
        l.setUser(user);
        em.persistAndFlush(l);
    }

    private void persistComment(Post post, User user, String content) {
        Comment c = new Comment();
        c.setPost(post);
        c.setUser(user);
        c.setContent(content);
        em.persistAndFlush(c);
    }

    private void persistFollow(User follower, User following) {
        Follow f = new Follow();
        f.setFollower(follower);
        f.setFollowing(following);
        em.persistAndFlush(f);
    }
}
