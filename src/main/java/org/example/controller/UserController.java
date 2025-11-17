package org.example.controller;


import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.UserUpdateDto;
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId, @AuthenticationPrincipal UserDetails currentUserDetails) {
        log.info("GET /api/user/{} - Uživatel {} požaduje informace o uživateli", userId, currentUserDetails.getUsername());
        Optional<User> authenticatedUserOpt = userService.findUserByUsername(currentUserDetails.getUsername());
        if( authenticatedUserOpt.isEmpty()) {
            log.error("Interní chyba: Autentizovaný uživatel {} nebyl nalezen v databázi.", currentUserDetails.getUsername());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User authenticatedUser = authenticatedUserOpt.get();
        if( !authenticatedUser.getId().equals(userId) && !currentUserDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            log.warn("Pokus o neoprávněný přístup: Uživatel {} se pokusil získat informace o uživateli {}", currentUserDetails.getUsername(), userId);
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
        log.info("PUT /api/user/{} - Uživatel {} požaduje aktualizaci dat: {}", userId, currentUserDetails.getUsername(), userUpdateDto);
        Optional<User> existingUserOpt = userService.findUserById(userId);
        if (existingUserOpt.isEmpty()) {
            log.warn("Aktualizace se nezdařila: Uživatel s ID {} nebyl nalezen.", userId);
            return ResponseEntity.notFound().build();
        }

        User existingUser = existingUserOpt.get();
        Optional<User> authenticatedUserOpt = userService.findUserByUsername(currentUserDetails.getUsername());

        if (authenticatedUserOpt.isEmpty()) {
            log.error("Interní chyba: Autentizovaný uživatel {} nebyl nalezen v databázi.", currentUserDetails.getUsername());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User authenticatedUser = authenticatedUserOpt.get();
        boolean isAdmin = currentUserDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Ověření oprávnění
        if (!existingUser.getId().equals(authenticatedUser.getId()) && !isAdmin) {
            log.warn("Pokus o neoprávněný přístup: Uživatel {} se pokusil aktualizovat data uživatele {}", currentUserDetails.getUsername(), userId);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
        }

        // Aktualizace dat:
        existingUser.setEmail(userUpdateDto.getEmail());
        existingUser.setUsername(userUpdateDto.getUsername());



        // Pokud je uživatel admin, může měnit role
        if (isAdmin && userUpdateDto.getRole() != null) {
            log.info("Admin {} mění roli uživatele {} na {}", currentUserDetails.getUsername(), userId, userUpdateDto.getRole());
            existingUser.setRole(userUpdateDto.getRole());
        }

        User updatedUser = userService.save(existingUser);
        log.info("Data uživatele s ID {} byla úspěšně aktualizována.", userId);
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
