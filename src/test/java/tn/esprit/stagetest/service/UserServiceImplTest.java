package tn.esprit.stagetest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.stagetest.dto.RegistrationDto;
import tn.esprit.stagetest.entity.User;
import tn.esprit.stagetest.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegistrationDto registrationDto;
    private User user;

    @BeforeEach
    void setUp() {
        registrationDto = new RegistrationDto();
        registrationDto.setUsername("testuser");
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("password123");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();
    }

    @Test
    void registerUser_ReturnsSavedUser() {
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User saved = userService.registerUser(registrationDto);

        assertThat(saved).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");
        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
        
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void usernameExists_ReturnsTrue() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        boolean exists = userService.usernameExists("testuser");
        assertThat(exists).isTrue();
    }

    @Test
    void usernameExists_ReturnsFalse() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        boolean exists = userService.usernameExists("testuser");
        assertThat(exists).isFalse();
    }

    @Test
    void emailExists_ReturnsTrue() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        boolean exists = userService.emailExists("test@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    void emailExists_ReturnsFalse() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        boolean exists = userService.emailExists("test@example.com");
        assertThat(exists).isFalse();
    }
}
