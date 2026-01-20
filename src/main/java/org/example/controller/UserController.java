package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.UserUpdateResponse;
import jakarta.validation.Valid;
import org.example.dto.UserResponse;
import org.example.mapper.UserMapper;
import org.example.model.User;
import org.example.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Získání seznamu všech uživatelů.
     * Vyžaduje ROLE_ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        log.info("GET /api/user - Získání seznamu všech uživatelů");
        return userService.findAllUsers().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Získání informací o přihlášeném uživateli
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get currentUser", description = "Získání informací o přihlášeném uživateli") // Swagger
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uživatel nalezen"),
            @ApiResponse(responseCode = "404", description = "Uživatel nenalezen")
    })
    public ResponseEntity<UserResponse> getCurrentUser(@Valid @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/user/me - Získání informací o přihlášeném uživateli: {}", userDetails.getUsername());
        return userService.findUserByUsername(userDetails.getUsername())
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Získání uživatele podle ID.
     * Vyžaduje ROLE_ADMIN nebo vlastníka účtu.
     *
     * @param userId ID uživatele.
     * @return Uživatelská data nebo 404, pokud uživatel neexistuje.
     */

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#userId, principal.username)")
    @Operation(summary = "Get user by ID", description = "Požadavek na informace o uživateli.") // Swagger
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uživatel nalezen"),
            @ApiResponse(responseCode = "404", description = "Uživatel nenalezen")
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        log.info("GET /api/user/{} - Požadavek na informace o uživateli", userId);
        return userService.findUserById(userId)
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    /**
     * Smaže uživatele.
     * Vyžaduje ROLE_ADMIN.
     * 
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
    /**
     * ✅ Aktualizace uživatele
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#userId, principal.username)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody UserUpdateResponse request
    ) {
        log.info("Požadavek na update uživatele ID: {}", userId);
        // 1. Najde uživatele podle ID
        User existingUser = userService.findUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uživatel nenalezen"));
        // 2. Provede update
        User updatedUser = userService.updateUser(existingUser, request);
        // 3. Vrátí odpověď
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }
}
