package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ForgotPasswordRequest;
import org.example.dto.MessageResponse;
import org.example.dto.ResetPasswordRequest;
import org.example.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    /**
     * ✅ Iniciace resetu hesla - odeslání emailu s tokenem
     * @param forgotPasswordRequest
     * @return
     */
    @PostMapping("forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        passwordResetService.initiatePasswordReset(forgotPasswordRequest.email());
        return ResponseEntity.ok(new MessageResponse("Žádost o obnovení hesla byla odeslána na váš email"));
    }

    /**
     * ✅ Reset hesla pomocí tokenu
     * @param resetPasswordRequest
     * @return
     */
    @PostMapping("reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        try{
            passwordResetService.resetPassword(resetPasswordRequest.token(), resetPasswordRequest.newPassword());
            return ResponseEntity.ok(new MessageResponse("Heslo bylo úspěšně změněno."));
        } catch (IllegalArgumentException e) {
            log.error("Chyba při resetu hesla s tokenem {}: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
