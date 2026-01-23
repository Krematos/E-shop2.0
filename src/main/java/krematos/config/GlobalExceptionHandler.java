package krematos.config;

import krematos.dto.MessageResponse;
import krematos.exception.email.EmailAlreadyExistsException;
import krematos.exception.product.ProductNotFoundException;
import krematos.exception.token.InvalidTokenException;
import krematos.exception.token.TokenExpiredException;
import krematos.exception.user.UserAlreadyExistException;
import krematos.exception.user.UserNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.security.auth.login.AccountLockedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Object> handleUserAlreadyExistException(UserAlreadyExistException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
    /**
     * VALIDACE VSTUPŮ (@Valid) - 400 Bad Request
     * Toto se spustí, když klient pošle neplatná data (např. krátké heslo, špatný email).
     * Místo jedné chyby vrátí mapu všech chyb (pole -> chyba).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * CHYBA FORMÁTU JSON - 400 Bad Request
     * Spustí se, když klient pošle poškozený JSON (např. chybějící čárka,
     * nebo posílá String do pole, které očekává Integer).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonErrors(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Neplatný formát JSON požadavku. Zkontrolujte syntaxi a datové typy."));
    }

    // 401 - Špatné heslo nebo jméno
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Neplatné přihlašovací údaje")); // Zpráva je generická schválně
    }

    /**
     * ✅ Řeší chyby @PreAuthorize (chybějící role)
     * Vrací 403 Forbidden místo 500.
     */
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Přístup odepřen: Nemáte dostatečná oprávnění."));
    }

    /**
     * 3. DUPLICITNÍ DATA V DB - 409 Conflict
     * Spustí se, když se snažíte uložit entitu s unikátním polem, které už existuje.
     * (Např. registrace emailu, který už v DB je, a validace v Service vrstvě to nezachytila).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Operaci nelze provést z důvodu konfliktu dat (např. duplicitní záznam)."));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
    // 415 - Nepodporovaný formát dat
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(Map.of("error", "Nepodporovaný formát dat: " + ex.getMessage()));
    }

    // 423 - Účet uzamčen
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, String>> handleLocked(AccountLockedException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(Map.of("error", ex.getMessage()));
    }

    // 404 - Nenalezeno (např. pro admina)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // Řeší expiraci tokenu -> 400
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<MessageResponse> handleTokenExpired(TokenExpiredException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Odkaz pro obnovu hesla již vypršel. Vyžádejte si prosím nový."));
    }


    // Řeší neplatný token -> 400 (nebo 404)
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<MessageResponse> handleInvalidToken(InvalidTokenException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Neplatný odkaz pro obnovu hesla."));
    }

    // Obecná výjimka pro ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("error", ex.getReason()));
    }

    /**
     * ✅ Řeší IllegalArgumentException (např. duplicitní email, neplatné argumenty)
     * Vrací 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * ✅ Řeší situaci, kdy endpoint neexistuje (404).
     * Zabrání tomu, aby to spadlo do obecné 500 chyby.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Endpoint nebo zdroj nenalezen: " + ex.getResourcePath()));
    }

    // 500 - Všechny ostatní chyby serveru
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Chyba serveru: " + ex.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFoundException(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

}
