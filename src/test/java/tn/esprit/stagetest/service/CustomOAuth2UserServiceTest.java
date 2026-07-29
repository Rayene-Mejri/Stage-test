package tn.esprit.stagetest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import tn.esprit.stagetest.entity.User;
import tn.esprit.stagetest.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2UserRequest userRequest;

    @Mock
    private OAuth2User mockOAuth2User;

    @InjectMocks
    @Spy
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void testLoadUser_NewGoogleUser() {
        doReturn(mockOAuth2User).when(customOAuth2UserService).loadOAuth2User(userRequest);
        when(mockOAuth2User.getAttribute("email")).thenReturn("newuser@gmail.com");
        when(mockOAuth2User.getAttribute("name")).thenReturn("New User");
        when(userRepository.findByEmail("newuser@gmail.com")).thenReturn(Optional.empty());

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        assertThat(result).isEqualTo(mockOAuth2User);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoadUser_ExistingUserNotGoogle() {
        doReturn(mockOAuth2User).when(customOAuth2UserService).loadOAuth2User(userRequest);
        when(mockOAuth2User.getAttribute("email")).thenReturn("existing@gmail.com");
        
        User existingUser = User.builder()
                .id(1L)
                .email("existing@gmail.com")
                .authProvider("LOCAL")
                .build();
        
        when(userRepository.findByEmail("existing@gmail.com")).thenReturn(Optional.of(existingUser));

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        assertThat(result).isEqualTo(mockOAuth2User);
        verify(userRepository, times(1)).save(existingUser);
        assertThat(existingUser.getAuthProvider()).isEqualTo("GOOGLE");
    }

    @Test
    void testLoadUser_ExistingGoogleUser() {
        doReturn(mockOAuth2User).when(customOAuth2UserService).loadOAuth2User(userRequest);
        when(mockOAuth2User.getAttribute("email")).thenReturn("googleuser@gmail.com");
        
        User existingUser = User.builder()
                .id(2L)
                .email("googleuser@gmail.com")
                .authProvider("GOOGLE")
                .build();
        
        when(userRepository.findByEmail("googleuser@gmail.com")).thenReturn(Optional.of(existingUser));

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        assertThat(result).isEqualTo(mockOAuth2User);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoadUser_EmailWithoutDomain() {
        doReturn(mockOAuth2User).when(customOAuth2UserService).loadOAuth2User(userRequest);
        when(mockOAuth2User.getAttribute("email")).thenReturn("nodomain");
        when(mockOAuth2User.getAttribute("name")).thenReturn("No Domain User");
        when(userRepository.findByEmail("nodomain")).thenReturn(Optional.empty());

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        assertThat(result).isEqualTo(mockOAuth2User);
        verify(userRepository, times(1)).save(argThat(user -> "No Domain User".equals(user.getUsername())));
    }
}
