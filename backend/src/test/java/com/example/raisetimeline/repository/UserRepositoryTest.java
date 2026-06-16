package com.example.raisetimeline.repository;

import com.example.raisetimeline.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = createUser("alice@example.com", "alice", "Alice");
        user2 = createUser("bob@example.com", "bob_user", "Bob");
        em.persistAndFlush(user1);
        em.persistAndFlush(user2);
    }

    // --- findByEmail ---

    @Test
    void findByEmail_existingEmail_returnsUser() {
        Optional<User> result = userRepository.findByEmail("alice@example.com");
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
    }

    @Test
    void findByEmail_unknownEmail_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("nobody@example.com");
        assertThat(result).isEmpty();
    }

    // --- existsByEmail ---

    @Test
    void existsByEmail_existingEmail_returnsTrue() {
        assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
    }

    @Test
    void existsByEmail_unknownEmail_returnsFalse() {
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    // --- searchByUsername ---

    @Test
    void searchByUsername_exactMatch_returnsUser() {
        List<User> results = userRepository.searchByUsername("alice", user2.getId());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("alice");
    }

    @Test
    void searchByUsername_partialMatch_returnsMatchingUsers() {
        List<User> results = userRepository.searchByUsername("bob", user1.getId());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("bob_user");
    }

    @Test
    void searchByUsername_caseInsensitive_returnsResults() {
        List<User> results = userRepository.searchByUsername("ALICE", user2.getId());
        assertThat(results).hasSize(1);
    }

    @Test
    void searchByUsername_excludesCurrentUser() {
        List<User> results = userRepository.searchByUsername("alice", user1.getId());
        assertThat(results).isEmpty();
    }

    @Test
    void searchByUsername_noMatch_returnsEmptyList() {
        List<User> results = userRepository.searchByUsername("zzz", user1.getId());
        assertThat(results).isEmpty();
    }

    // --- helper ---

    private User createUser(String email, String username, String displayName) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("hashed");
        u.setUsername(username);
        u.setDisplayName(displayName);
        return u;
    }
}
