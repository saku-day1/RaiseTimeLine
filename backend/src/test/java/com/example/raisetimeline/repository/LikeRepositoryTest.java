package com.example.raisetimeline.repository;

import com.example.raisetimeline.entity.Like;
import com.example.raisetimeline.entity.Post;
import com.example.raisetimeline.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class LikeRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    LikeRepository likeRepository;

    private User user1;
    private User user2;
    private Post post1;
    private Post post2;

    @BeforeEach
    void setUp() {
        user1 = createUser("u1@example.com", "user1");
        user2 = createUser("u2@example.com", "user2");
        em.persistAndFlush(user1);
        em.persistAndFlush(user2);

        post1 = persistPost(user1, "post1");
        post2 = persistPost(user1, "post2");
    }

    // --- existsByPostIdAndUserId ---

    @Test
    void existsByPostIdAndUserId_liked_returnsTrue() {
        persistLike(post1, user2);
        assertThat(likeRepository.existsByPostIdAndUserId(post1.getId(), user2.getId())).isTrue();
    }

    @Test
    void existsByPostIdAndUserId_notLiked_returnsFalse() {
        assertThat(likeRepository.existsByPostIdAndUserId(post1.getId(), user2.getId())).isFalse();
    }

    // --- countByPostId ---

    @Test
    void countByPostId_noLikes_returnsZero() {
        assertThat(likeRepository.countByPostId(post1.getId())).isEqualTo(0L);
    }

    @Test
    void countByPostId_withLikes_returnsCorrectCount() {
        persistLike(post1, user1);
        persistLike(post1, user2);
        assertThat(likeRepository.countByPostId(post1.getId())).isEqualTo(2L);
    }

    // --- findByPostIdAndUserId ---

    @Test
    void findByPostIdAndUserId_existing_returnsLike() {
        Like like = persistLike(post1, user2);
        Optional<Like> result = likeRepository.findByPostIdAndUserId(post1.getId(), user2.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(like.getId());
    }

    @Test
    void findByPostIdAndUserId_missing_returnsEmpty() {
        Optional<Like> result = likeRepository.findByPostIdAndUserId(post1.getId(), user2.getId());
        assertThat(result).isEmpty();
    }

    // --- findLikedPostIdsByUserIdAndPostIds ---

    @Test
    void findLikedPostIdsByUserIdAndPostIds_returnsOnlyLikedIds() {
        persistLike(post1, user2);

        Set<Long> result = likeRepository.findLikedPostIdsByUserIdAndPostIds(
                user2.getId(), List.of(post1.getId(), post2.getId()));
        assertThat(result).containsExactly(post1.getId());
    }

    @Test
    void findLikedPostIdsByUserIdAndPostIds_emptyPostIds_returnsEmptySet() {
        Set<Long> result = likeRepository.findLikedPostIdsByUserIdAndPostIds(user2.getId(), List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void findLikedPostIdsByUserIdAndPostIds_noLikedPosts_returnsEmptySet() {
        Set<Long> result = likeRepository.findLikedPostIdsByUserIdAndPostIds(
                user2.getId(), List.of(post1.getId(), post2.getId()));
        assertThat(result).isEmpty();
    }

    @Test
    void findLikedPostIdsByUserIdAndPostIds_multiplePostsLiked_returnsAllLikedIds() {
        persistLike(post1, user2);
        persistLike(post2, user2);

        Set<Long> result = likeRepository.findLikedPostIdsByUserIdAndPostIds(
                user2.getId(), List.of(post1.getId(), post2.getId()));
        assertThat(result).containsExactlyInAnyOrder(post1.getId(), post2.getId());
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

    private Like persistLike(Post post, User user) {
        Like l = new Like();
        l.setPost(post);
        l.setUser(user);
        return em.persistAndFlush(l);
    }
}
