package io.github.jhipster.sample.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jhipster.sample.IntegrationTest;
import io.github.jhipster.sample.domain.Authority;
import io.github.jhipster.sample.domain.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link UserRepository}.
 */
@IntegrationTest
@Transactional
class UserRepositoryTest {

    private static final String DEFAULT_LOGIN = "testuser_repo";
    private static final String DEFAULT_EMAIL = "testuser_repo@localhost";
    private static final String DEFAULT_FIRSTNAME = "Test";
    private static final String DEFAULT_LASTNAME = "User";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    private User user;
    private Long numberOfUsers;

    @BeforeEach
    void countUsers() {
        numberOfUsers = userRepository.count();
    }

    @BeforeEach
    void init() {
        user = new User();
        user.setLogin(DEFAULT_LOGIN);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail(DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setLangKey("en");
    }

    @AfterEach
    void cleanup() {
        if (user != null && user.getId() != null) {
            userRepository.deleteById(user.getId());
        }
        assertThat(userRepository.count()).isEqualTo(numberOfUsers);
        numberOfUsers = null;
    }

    @Test
    @Transactional
    void assertThatFindOneByActivationKeyWorks() {
        user.setActivated(false);
        user.setActivationKey("activation-key-123");
        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findOneByActivationKey("activation-key-123");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.orElseThrow().getLogin()).isEqualTo(DEFAULT_LOGIN);
    }

    @Test
    @Transactional
    void assertThatFindOneByActivationKeyReturnsEmptyForInvalidKey() {
        user.setActivated(false);
        user.setActivationKey("activation-key-123");
        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findOneByActivationKey("invalid-key");
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Transactional
    void assertThatFindOneByResetKeyWorks() {
        user.setResetKey("reset-key-123");
        user.setResetDate(Instant.now());
        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findOneByResetKey("reset-key-123");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.orElseThrow().getLogin()).isEqualTo(DEFAULT_LOGIN);
    }

    @Test
    @Transactional
    void assertThatFindOneByResetKeyReturnsEmptyForInvalidKey() {
        user.setResetKey("reset-key-123");
        user.setResetDate(Instant.now());
        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findOneByResetKey("invalid-key");
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Transactional
    void assertThatFindOneByEmailIgnoreCaseWorks() {
        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findOneByEmailIgnoreCase(DEFAULT_EMAIL.toUpperCase());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.orElseThrow().getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    @Transactional
    void assertThatFindOneByEmailIgnoreCaseReturnsEmptyForNonExistent() {
        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findOneByEmailIgnoreCase("nonexistent@example.com");
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Transactional
    void assertThatFindOneByLoginWorks() {
        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findOneByLogin(DEFAULT_LOGIN);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.orElseThrow().getLogin()).isEqualTo(DEFAULT_LOGIN);
    }

    @Test
    @Transactional
    void assertThatFindOneByLoginReturnsEmptyForNonExistent() {
        Optional<User> foundUser = userRepository.findOneByLogin("nonexistent");
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Transactional
    void assertThatFindOneWithAuthoritiesByLoginWorks() {
        Authority authority = authorityRepository.findById("ROLE_USER").orElseThrow();
        Set<Authority> authorities = new HashSet<>();
        authorities.add(authority);
        user.setAuthorities(authorities);
        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findOneWithAuthoritiesByLogin(DEFAULT_LOGIN);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.orElseThrow().getAuthorities()).isNotEmpty();
    }

    @Test
    @Transactional
    void assertThatFindOneWithAuthoritiesByEmailIgnoreCaseWorks() {
        Authority authority = authorityRepository.findById("ROLE_USER").orElseThrow();
        Set<Authority> authorities = new HashSet<>();
        authorities.add(authority);
        user.setAuthorities(authorities);
        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findOneWithAuthoritiesByEmailIgnoreCase(DEFAULT_EMAIL);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.orElseThrow().getAuthorities()).isNotEmpty();
    }

    @Test
    @Transactional
    void assertThatFindAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBeforeWorks() {
        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);
        user.setActivated(false);
        user.setActivationKey("activation-key");
        user.setCreatedDate(threeDaysAgo);
        userRepository.saveAndFlush(user);

        List<User> foundUsers = userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(
            Instant.now().minus(2, ChronoUnit.DAYS)
        );
        assertThat(foundUsers).isNotEmpty();
        assertThat(foundUsers.stream().anyMatch(u -> u.getLogin().equals(DEFAULT_LOGIN))).isTrue();
    }

    @Test
    @Transactional
    void assertThatFindAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBeforeReturnsEmptyForRecentUsers() {
        user.setActivated(false);
        user.setActivationKey("activation-key");
        user.setCreatedDate(Instant.now().minus(1, ChronoUnit.DAYS));
        userRepository.saveAndFlush(user);

        List<User> foundUsers = userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(
            Instant.now().minus(2, ChronoUnit.DAYS)
        );
        assertThat(foundUsers).isEmpty();
    }

    @Test
    @Transactional
    void assertThatFindAllByIdNotNullAndActivatedIsTrueWorks() {
        user.setActivated(true);
        userRepository.saveAndFlush(user);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> users = userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable);

        assertThat(users.getContent()).isNotEmpty();
        assertThat(users.getContent().stream().anyMatch(u -> u.getLogin().equals(DEFAULT_LOGIN))).isTrue();
    }

    @Test
    @Transactional
    void assertThatFindAllByIdNotNullAndActivatedIsTrueExcludesNonActivated() {
        user.setActivated(false);
        userRepository.saveAndFlush(user);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> users = userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable);

        assertThat(users.getContent().stream().anyMatch(u -> u.getLogin().equals(DEFAULT_LOGIN))).isFalse();
    }
}
