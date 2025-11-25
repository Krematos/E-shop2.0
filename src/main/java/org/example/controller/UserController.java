package org.example.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.UserUpdateDto;
import jakarta.validation.Valid;
import org.example.dto.UserDto;
import org.example.model.User;
import org.example.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Získání seznamu všech uživatelů.
     * Vyžaduje ROLE_ADMIN.
     */

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public List<UserDto> getAllUsers() {
        log.info("GET /api/user - Získání seznamu všech uživatelů");
        return userService.findAllUsers().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Získání informací o přihlášeném uživateli
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/user/me - Získání informací o přihlášeném uživateli: {}", userDetails.getUsername());
        return userService.findUserByUsername(userDetails.getUsername())
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#userId, principal.username)")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        log.info("GET /api/user/{} - Požadavek na informace o uživateli", userId);
        return userService.findUserById(userId)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#userId, principal.username)")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId,
                                              @Valid @RequestBody UserUpdateDto userUpdateDto) {
        log.info("PUT /api/user/{} - Požadavek na aktualizaci dat: {}", userId, userUpdateDto);
        Optional<User> existingUserOpt = userService.findUserById(userId);
        if (existingUserOpt.isEmpty()) {
            log.warn("Aktualizace se nezdařila: Uživatel s ID {} nebyl nalezen.", userId);
            return ResponseEntity.notFound().build();
        }

        User existingUser = existingUserOpt.get();

        // Aktualizace dat:
        try {
            User updatedUser = userService.updateUser(existingUser, userUpdateDto);
            log.info("Data uživatele s ID {} byla úspěšně aktualizována.", userId);
            return ResponseEntity.ok(convertToDto(updatedUser));
        } catch (IllegalArgumentException e) {
            log.warn("Aktualizace se nezdařila pro uživatele {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(null); // Or a DTO with the error message
        }
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
        log.info("DELETE /api/user/{} - Požadavek na smazání uživatele", userId);
        if (userService.findUserById(userId).isPresent()) {
            userService.DeleteUserById(userId);
            log.info("Uživatel s ID {} byl úspěšně smazán.", userId);
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        log.warn("Smazání se nezdařilo: Uživatel s ID {} nebyl nalezen.", userId);
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
