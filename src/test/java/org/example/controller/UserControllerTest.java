package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.SecurityConfig;
import org.example.dto.UserResponse;
import org.example.dto.UserUpdateResponse;
import org.example.mapper.UserMapper;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.security.JwtAuthenticationFilter;
import org.example.service.JwtService;
import org.example.service.impl.UserDetailsServiceImpl;
import org.example.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test pro UserController.
 * Testuje všechny REST API endpointy pro správu uživatelů.
 */
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private User adminUser;
    private UserResponse testUserResponse;
    private UserResponse adminUserResponse;

    @BeforeEach
    void setUp() {
        // Vytvoření testovacích uživatelů
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword123")
                .roles(Set.of(Role.USER))
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .password("encodedAdminPass")
                .roles(Set.of(Role.ADMIN))
                .build();

        // Vytvoření testovacích DTO responses
        testUserResponse = new UserResponse(
                1L,
                "testuser",
                "test@example.com",
                "encodedPassword123",
                "ROLE_USER");

        adminUserResponse = new UserResponse(
                2L,
                "admin",
                "admin@example.com",
                "encodedAdminPass",
                "ROLE_ADMIN");
    }

    // ==================== GET /api/user - getAllUsers ====================

    @Test
    @DisplayName("GET /api/user - Admin úspěšně získá seznam všech uživatelů")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_AsAdmin_ReturnsUserList() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser, adminUser);
        when(userService.findAllUsers()).thenReturn(users);
        when(userMapper.toDto(testUser)).thenReturn(testUserResponse);
        when(userMapper.toDto(adminUser)).thenReturn(adminUserResponse);

        // When & Then
        mockMvc.perform(get("/api/user"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[1].username").value("admin"));

        verify(userService, times(1)).findAllUsers();
    }

    @Test
    @DisplayName("GET /api/user - Běžný uživatel nemá přístup (403 Forbidden)")
    @WithMockUser(roles = "USER")
    void getAllUsers_AsUser_ReturnsForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/user"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).findAllUsers();
    }

    @Test
    @DisplayName("GET /api/user - Nepřihlášený uživatel nemá přístup (401 Unauthorized)")
    void getAllUsers_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/user"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, never()).findAllUsers();
    }

    // ==================== GET /api/user/me - getCurrentUser ====================

    @Test
    @DisplayName("GET /api/user/me - Uživatel úspěšně získá své údaje")
    @WithMockUser(username = "testuser", roles = "USER")
    void getCurrentUser_Success_ReturnsUserData() throws Exception {
        // Given
        when(userService.findUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(get("/api/user/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        verify(userService, times(1)).findUserByUsername("testuser");
    }

    @Test
    @DisplayName("GET /api/user/me - Uživatel nenalezen (404 Not Found)")
    @WithMockUser(username = "nonexistent")
    void getCurrentUser_NotFound_Returns404() throws Exception {
        // Given
        when(userService.findUserByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/user/me"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findUserByUsername("nonexistent");
    }

    // ==================== GET /api/user/{userId} - getUserById
    // ====================

    @Test
    @DisplayName("GET /api/user/{userId} - Admin úspěšně získá uživatele podle ID")
    @WithMockUser(roles = "ADMIN")
    void getUserById_AsAdmin_ReturnsUser() throws Exception {
        // Given
        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(get("/api/user/{userId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).findUserById(1L);
    }

    @Test
    @DisplayName("GET /api/user/{userId} - Vlastník úspěšně získá své údaje")
    @WithMockUser(username = "testuser", roles = "USER")
    void getUserById_AsOwner_ReturnsOwnData() throws Exception {
        // Given
        when(userService.isOwner(1L, "testuser")).thenReturn(true);
        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(get("/api/user/{userId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("GET /api/user/{userId} - Uživatel nenalezen (404)")
    @WithMockUser(roles = "ADMIN")
    void getUserById_NotFound_Returns404() throws Exception {
        // Given
        when(userService.findUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/user/{userId}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findUserById(999L);
    }

    // ==================== PUT /api/user/{userId} - updateUser ====================

    @Test
    @DisplayName("PUT /api/user/{userId} - Admin úspěšně aktualizuje uživatele")
    @WithMockUser(roles = "ADMIN")
    void updateUser_AsAdmin_Success() throws Exception {
        // Given
        UserUpdateResponse updateRequest = new UserUpdateResponse(
                "Jan",
                "Novák",
                "jan.novak@example.com");

        User updatedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("jan.novak@example.com")
                .password("encodedPassword123")
                .roles(Set.of(Role.USER))
                .build();

        UserResponse updatedResponse = new UserResponse(
                1L,
                "testuser",
                "jan.novak@example.com",
                "encodedPassword123",
                "ROLE_USER");

        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));
        when(userService.updateUser(eq(testUser), any(UserUpdateResponse.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/user/{userId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jan.novak@example.com"));

        verify(userService, times(1)).updateUser(eq(testUser), any(UserUpdateResponse.class));
    }

    @Test
    @DisplayName("PUT /api/user/{userId} - Vlastník úspěšně aktualizuje své údaje")
    @WithMockUser(username = "testuser", roles = "USER")
    void updateUser_AsOwner_Success() throws Exception {
        // Given
        UserUpdateResponse updateRequest = new UserUpdateResponse(
                "Jan",
                "Novák",
                "new.email@example.com");

        when(userService.isOwner(1L, "testuser")).thenReturn(true);
        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));
        when(userService.updateUser(eq(testUser), any(UserUpdateResponse.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(put("/api/user/{userId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(eq(testUser), any(UserUpdateResponse.class));
    }

    @Test
    @DisplayName("PUT /api/user/{userId} - Uživatel nenalezen (404)")
    @WithMockUser(roles = "ADMIN")
    void updateUser_NotFound_Returns404() throws Exception {
        // Given
        UserUpdateResponse updateRequest = new UserUpdateResponse(
                "Jan",
                "Novák",
                "test@example.com");

        when(userService.findUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/user/{userId}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    @DisplayName("PUT /api/user/{userId} - Duplicitní email vrátí 400 Bad Request")
    @WithMockUser(roles = "ADMIN")
    void updateUser_DuplicateEmail_ReturnsBadRequest() throws Exception {
        // Given
        UserUpdateResponse updateRequest = new UserUpdateResponse(
                "Jan",
                "Novák",
                "duplicate@example.com");

        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));
        when(userService.updateUser(eq(testUser), any(UserUpdateResponse.class)))
                .thenThrow(new IllegalArgumentException("Email již existuje"));

        // When & Then
        mockMvc.perform(put("/api/user/{userId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(testUser), any(UserUpdateResponse.class));
    }

    @Test
    @DisplayName("PUT /api/user/{userId} - Neplatná validace (prázdné hodnoty)")
    @WithMockUser(roles = "ADMIN")
    void updateUser_InvalidData_ReturnsBadRequest() throws Exception {
        // Given - prázdný firstName
        UserUpdateResponse invalidRequest = new UserUpdateResponse(
                "", // prázdné jméno
                "Novák",
                "test@example.com");

        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(put("/api/user/{userId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ==================== DELETE /api/user/{userId} - deleteUser
    // ====================

    @Test
    @DisplayName("DELETE /api/user/{userId} - Admin úspěšně smaže uživatele")
    @WithMockUser(roles = "ADMIN")
    void deleteUser_AsAdmin_Success() throws Exception {
        // Given
        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userService).DeleteUserById(1L);

        // When & Then
        mockMvc.perform(delete("/api/user/{userId}", 1L))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userService, times(1)).DeleteUserById(1L);
    }

    @Test
    @DisplayName("DELETE /api/user/{userId} - Uživatel nenalezen (404)")
    @WithMockUser(roles = "ADMIN")
    void deleteUser_NotFound_Returns404() throws Exception {
        // Given
        when(userService.findUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/user/{userId}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, never()).DeleteUserById(anyLong());
    }

    @Test
    @DisplayName("DELETE /api/user/{userId} - Běžný uživatel nemá přístup (403)")
    @WithMockUser(roles = "USER")
    void deleteUser_AsUser_ReturnsForbidden() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/user/{userId}", 1L))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).DeleteUserById(anyLong());
    }
}
