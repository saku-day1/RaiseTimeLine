package com.example.raisetimeline.repository;

import com.example.raisetimeline.entity.Follow;
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
class FollowRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    FollowRepository followRepository;

    private User userA;
    private User userB;
    private User userC;

    @BeforeEach
    void setUp() {
        userA = createUser("a@example.com", "userA");
        userB = createUser("b@example.com", "userB");
        userC = createUser("c@example.com", "userC");
        em.persistAndFlush(userA);
        em.persistAndFlush(userB);
        em.persistAndFlush(userC);
    }

    // --- existsByFollowerIdAndFollowingId ---

    @Test
    void existsByFollowerIdAndFollowingId_existingFollow_returnsTrue() {
        persistFollow(userA, userB);
        assertThat(followRepository.existsByFollowerIdAndFollowingId(userA.getId(), userB.getId())).isTrue();
    }

    @Test
    void existsByFollowerIdAndFollowingId_noFollow_returnsFalse() {
        assertThat(followRepository.existsByFollowerIdAndFollowingId(userA.getId(), userB.getId())).isFalse();
    }

    // --- findByFollowerIdAndFollowingId ---

    @Test
    void findByFollowerIdAndFollowingId_existingFollow_returnsFollow() {
        Follow follow = persistFollow(userA, userB);
        Optional<Follow> result = followRepository.findByFollowerIdAndFollowingId(userA.getId(), userB.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(follow.getId());
    }

    @Test
    void findByFollowerIdAndFollowingId_noFollow_returnsEmpty() {
        Optional<Follow> result = followRepository.findByFollowerIdAndFollowingId(userA.getId(), userB.getId());
        assertThat(result).isEmpty();
    }

    // --- countByFollowingId / countByFollowerId ---

    @Test
    void countByFollowingId_correctCount() {
        persistFollow(userA, userB);
        persistFollow(userC, userB);
        assertThat(followRepository.countByFollowingId(userB.getId())).isEqualTo(2L);
    }

    @Test
    void countByFollowerId_correctCount() {
        persistFollow(userA, userB);
        persistFollow(userA, userC);
        assertThat(followRepository.countByFollowerId(userA.getId())).isEqualTo(2L);
    }

    // --- findFollowingIdsByFollowerId ---

    @Test
    void findFollowingIdsByFollowerId_returnsIds() {
        persistFollow(userA, userB);
        persistFollow(userA, userC);

        List<Long> ids = followRepository.findFollowingIdsByFollowerId(userA.getId());
        assertThat(ids).containsExactlyInAnyOrder(userB.getId(), userC.getId());
    }

    @Test
    void findFollowingIdsByFollowerId_noFollowing_returnsEmptyList() {
        List<Long> ids = followRepository.findFollowingIdsByFollowerId(userA.getId());
        assertThat(ids).isEmpty();
    }

    // --- findFollowersByFollowingId ---

    @Test
    void findFollowersByFollowingId_returnsFollowerEntities() {
        persistFollow(userA, userB);
        persistFollow(userC, userB);

        List<User> followers = followRepository.findFollowersByFollowingId(userB.getId());
        assertThat(followers).extracting(User::getId)
                .containsExactlyInAnyOrder(userA.getId(), userC.getId());
    }

    // --- findFollowingsByFollowerId ---

    @Test
    void findFollowingsByFollowerId_returnsFollowingEntities() {
        persistFollow(userA, userB);
        persistFollow(userA, userC);

        List<User> followings = followRepository.findFollowingsByFollowerId(userA.getId());
        assertThat(followings).extracting(User::getId)
                .containsExactlyInAnyOrder(userB.getId(), userC.getId());
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

    private Follow persistFollow(User follower, User following) {
        Follow f = new Follow();
        f.setFollower(follower);
        f.setFollowing(following);
        return em.persistAndFlush(f);
    }
}
