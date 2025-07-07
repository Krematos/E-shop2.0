package org.example.controller;


import org.example.dto.UserUpdateDto;
import org.example.repository.UserRepository;
import jakarta.validation.Valid;
import org.example.dto.UserDto;
import org.example.model.User;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    private Set<String> roles;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public List<UserDto> getAllUsers() {
        return userService.findAllUsers().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId, @AuthenticationPrincipal UserDetails currentUserDetails) {
        Optional<User> authenticatedUserOpt = userService.findUserByUsername(currentUserDetails.getUsername());
        if( authenticatedUserOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User authenticatedUser = authenticatedUserOpt.get();
        if( !authenticatedUser.getId().equals(userId) && !currentUserDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
        }
        return userService.findUserById(userId)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId,
                                              @Valid @RequestBody UserUpdateDto userUpdateDto,
                                              @AuthenticationPrincipal UserDetails currentUserDetails) {
        Optional<User> existingUserOpt = userService.findUserById(userId);
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = existingUserOpt.get();
        Optional<User> authenticatedUserOpt = userService.findUserByUsername(currentUserDetails.getUsername());

        if (authenticatedUserOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User authenticatedUser = authenticatedUserOpt.get();
        boolean isAdmin = currentUserDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Ověření oprávnění
        if (!existingUser.getId().equals(authenticatedUser.getId()) && !isAdmin) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
        }

        // Aktualizace dat:
        existingUser.setEmail(userUpdateDto.getEmail());
        existingUser.setUsername(userUpdateDto.getUsername());



        // Pokud je uživatel admin, může měnit role
        if (isAdmin && userUpdateDto.getRole() != null) {
            existingUser.setRole(userUpdateDto.getRole());
        }

        User updatedUser = userService.save(existingUser);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    /**
     * Smaže uživatele.
     * Vyžaduje ROLE_ADMIN.
     * @param userId ID uživatele ke smazání.
     * @return HTTP 204 No Content.
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        if (userService.findUserById(userId).isPresent()) {
            userService.DeleteUserById(userId);
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.notFound().build(); // 404 Not Found
    }

    // --- Pomocné metody pro konverzi mezi entitou a DTO ---

    /**
     * Konvertuje entitu User na UserDto.
     * Nezahrnuje heslo pro bezpečnostní důvody.
     */
    private UserDto convertToDto(User user) {
        if (user == null) {
            return null; // nebo throw new IllegalArgumentException("User cannot be null");
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        // Převod rolí z enum na Stringy pro DTO
        dto.setRoles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()).toString());
        return dto;
    }
}
