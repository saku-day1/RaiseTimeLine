package com.example.raisetimeline.repository;

import com.example.raisetimeline.entity.Comment;
import com.example.raisetimeline.entity.Post;
import com.example.raisetimeline.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CommentRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    CommentRepository commentRepository;

    private User author;
    private User commenter;
    private Post post;
    private Post otherPost;

    @BeforeEach
    void setUp() {
        author = createUser("author@example.com", "author");
        commenter = createUser("commenter@example.com", "commenter");
        em.persistAndFlush(author);
        em.persistAndFlush(commenter);

        post = persistPost(author, "main post");
        otherPost = persistPost(author, "other post");
    }

    // --- findByPostIdWithUser ---

    @Test
    void findByPostIdWithUser_returnsCommentsForPost() {
        persistComment(post, commenter, "first");
        persistComment(post, author, "second");

        List<Comment> result = commentRepository.findByPostIdWithUser(post.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    void findByPostIdWithUser_doesNotReturnCommentsFromOtherPosts() {
        persistComment(post, commenter, "on main");
        persistComment(otherPost, commenter, "on other");

        List<Comment> result = commentRepository.findByPostIdWithUser(post.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("on main");
    }

    @Test
    void findByPostIdWithUser_userFieldEagerlyLoaded() {
        persistComment(post, commenter, "hello");
        em.clear();

        List<Comment> result = commentRepository.findByPostIdWithUser(post.getId());
        assertThat(result).hasSize(1);
        // JOIN FETCH により N+1 なしにアクセス可能なこと
        assertThat(result.get(0).getUser().getUsername()).isEqualTo("commenter");
    }

    @Test
    void findByPostIdWithUser_emptyPost_returnsEmptyList() {
        List<Comment> result = commentRepository.findByPostIdWithUser(post.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void findByPostIdWithUser_orderedByCreatedAtDesc() throws InterruptedException {
        persistComment(post, commenter, "older");
        Thread.sleep(10);
        persistComment(post, author, "newer");

        List<Comment> result = commentRepository.findByPostIdWithUser(post.getId());
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("newer");
    }

    // --- existsByIdAndUserId ---

    @Test
    void existsByIdAndUserId_matching_returnsTrue() {
        Comment c = persistComment(post, commenter, "mine");
        assertThat(commentRepository.existsByIdAndUserId(c.getId(), commenter.getId())).isTrue();
    }

    @Test
    void existsByIdAndUserId_wrongUser_returnsFalse() {
        Comment c = persistComment(post, commenter, "mine");
        assertThat(commentRepository.existsByIdAndUserId(c.getId(), author.getId())).isFalse();
    }

    @Test
    void existsByIdAndUserId_unknownComment_returnsFalse() {
        assertThat(commentRepository.existsByIdAndUserId(99999L, commenter.getId())).isFalse();
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

    private Comment persistComment(Post post, User user, String content) {
        Comment c = new Comment();
        c.setPost(post);
        c.setUser(user);
        c.setContent(content);
        return em.persistAndFlush(c);
    }
}
