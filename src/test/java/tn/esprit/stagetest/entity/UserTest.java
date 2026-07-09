package tn.esprit.stagetest.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void testUserBuilder() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .build();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testUserNoArgsConstructor() {
        User user = new User();

        user.setId(2L);
        user.setUsername("johndoe");
        user.setPassword("secret");
        user.setEmail("john@example.com");

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getUsername()).isEqualTo("johndoe");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testUserAllArgsConstructor() {
        // Order: id, username, password, email (matches entity field order)
        User user = new User(3L, "alice", "pass123", "alice@example.com");

        assertThat(user.getId()).isEqualTo(3L);
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getPassword()).isEqualTo("pass123");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void testUserEquality() {
        User user1 = User.builder().id(1L).username("test").build();
        User user2 = User.builder().id(1L).username("test").build();

        assertThat(user1).isNotEqualTo(user2);
    }
}