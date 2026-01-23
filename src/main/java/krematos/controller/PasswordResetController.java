package krematos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import krematos.dto.MessageResponse;
import krematos.dto.password.ForgotPasswordRequest;
import krematos.dto.password.ResetPasswordRequest;
import krematos.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller pro správu obnovení hesla.
 * Poskytuje endpointy pro vyžádání reset tokenu přes email a obnovu hesla.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
@Tag(name = "Obnovení hesla", description = "API pro obnovení zapomenutého hesla (požadavek tokenu, reset hesla)")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    /**
     * 📧 Iniciace resetu hesla - odeslání emailu s tokenem
     *
     * @param forgotPasswordRequest Požadavek obsahující email uživatele
     * @return Potvrzení o odeslání emailu
     */
    @Operation(summary = "Požadavek na obnovení hesla", description = "Zašle email s odkazem pro reset hesla na zadanou emailovou adresu. "
            +
            "Email obsahuje jedinečný token platný po určitou dobu. " +
            "Tento endpoint vždy vrátí status 200, i když email neexistuje (z bezpečnostních důvodů).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Požadavek byl přijat, email byl odeslán (pokud účet existuje)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class), examples = @ExampleObject(value = "{\"message\":\"Žádost o obnovení hesla byla odeslána na váš email\"}"))),
            @ApiResponse(responseCode = "400", description = "Neplatný formát emailu", content = @Content)
    })
    @PostMapping("forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email uživatele pro obnovení hesla", required = true, content = @Content(schema = @Schema(implementation = ForgotPasswordRequest.class), examples = @ExampleObject(value = "{\"email\":\"user@example.com\"}"))) @RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        passwordResetService.initiatePasswordReset(forgotPasswordRequest.email());
        return ResponseEntity.ok(new MessageResponse("Žádost o obnovení hesla byla odeslána na váš email"));
    }

    /**
     * 🔑 Reset hesla pomocí tokenu
     *
     * @param resetPasswordRequest Požadavek obsahující reset token a nové heslo
     * @return Potvrzení o úspěšné změně hesla
     */
    @Operation(summary = "Reset hesla", description = "Provede obnovu hesla pomocí tokenu získaného z emailu. " +
            "Token musí být platný a nepropadlý. " +
            "Nové heslo musí splňovat bezpečnostní požadavky (minimální délka, složitost).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Heslo bylo úspěšně změněno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class), examples = @ExampleObject(value = "{\"message\":\"Heslo bylo úspěšně změněno.\"}"))),
            @ApiResponse(responseCode = "400", description = "Neplatný nebo propadlý token, nebo heslo nesplňuje požadavky", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "Neplatný token", value = "{\"error\":\"Reset token je neplatný nebo vypršel\"}"),
                    @ExampleObject(name = "Slabé heslo", value = "{\"error\":\"Heslo musí mít alespoň 8 znaků\"}")
            }))
    })
    @PostMapping("reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Reset token a nové heslo", required = true, content = @Content(schema = @Schema(implementation = ResetPasswordRequest.class), examples = @ExampleObject(value = "{\"token\":\"abc123xyz...\",\"newPassword\":\"NewSecurePassword123\"}"))) @RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        passwordResetService.resetPassword(resetPasswordRequest.token(), resetPasswordRequest.newPassword());
        return ResponseEntity.ok(new MessageResponse("Heslo bylo úspěšně změněno."));
    }
}
