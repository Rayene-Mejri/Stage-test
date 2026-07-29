package tn.esprit.stagetest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.stagetest.dto.RegistrationDto;
import tn.esprit.stagetest.service.*;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private DepartmentService departmentService;
    @MockitoBean
    private EmployeeService employeeService;
    @MockitoBean
    private ProjectService projectService;
    @MockitoBean
    private EquipmentService equipmentService;

    @Test
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("register"));
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        when(userService.usernameExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("email", "test@test.com")
                        .param("password", "Password123!")
                        .param("firstName", "Test")
                        .param("lastName", "User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(userService, times(1)).registerUser(any(RegistrationDto.class));
    }

    @Test
    void testRegisterUser_UsernameExists() throws Exception {
        when(userService.usernameExists(anyString())).thenReturn(true);
        when(userService.emailExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("email", "test@test.com")
                        .param("password", "Password123!")
                        .param("firstName", "Test")
                        .param("lastName", "User"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("user", "username", "error.user"))
                .andExpect(view().name("register"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void testRegisterUser_EmailExists() throws Exception {
        when(userService.usernameExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("email", "test@test.com")
                        .param("password", "Password123!")
                        .param("firstName", "Test")
                        .param("lastName", "User"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "error.user"))
                .andExpect(view().name("register"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void testRegisterUser_ValidationErrors() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "") // invalid empty username
                        .param("email", "invalidemail")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("register"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void testHomePage_Unauthenticated() throws Exception {
        when(departmentService.count()).thenReturn(10L);
        when(employeeService.count()).thenReturn(20L);
        when(projectService.count()).thenReturn(5L);
        when(equipmentService.count()).thenReturn(15L);

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("username", "User"))
                .andExpect(model().attribute("departmentCount", 10L))
                .andExpect(model().attribute("employeeCount", 20L))
                .andExpect(model().attribute("projectCount", 5L))
                .andExpect(model().attribute("equipmentCount", 15L))
                .andExpect(view().name("home"));
    }

    @Test
    void testHomePage_WithUserDetails() throws Exception {
        UserDetails userDetails = User.withUsername("customUser").password("pass").authorities("USER").build();

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        mockMvc.perform(get("/home").principal(auth))
                .andExpect(status().isOk())
                .andExpect(model().attribute("username", "customUser"))
                .andExpect(view().name("home"));
    }

    @Test
    void testHomePage_WithOAuth2User() throws Exception {
        OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                Map.of("name", "OAuthUser"),
                "name"
        );

        org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken auth = 
            new org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken(oauth2User, java.util.Collections.emptyList(), "clientRegistrationId");
        mockMvc.perform(get("/home").principal(auth))
                .andExpect(status().isOk())
                .andExpect(model().attribute("username", "OAuthUser"))
                .andExpect(view().name("home"));
    }
    
    @Test
    void testHomePage_WithOAuth2UserFallbackEmail() throws Exception {
        OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                Map.of("email", "oauth@test.com", "sub", "subject"),
                "sub"
        );

        org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken auth = 
            new org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken(oauth2User, java.util.Collections.emptyList(), "clientRegistrationId");
        mockMvc.perform(get("/home").principal(auth))
                .andExpect(status().isOk())
                .andExpect(model().attribute("username", "oauth@test.com"))
                .andExpect(view().name("home"));
    }
}
