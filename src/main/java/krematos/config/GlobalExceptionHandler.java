package krematos.config;

import jakarta.servlet.http.HttpServletRequest;
import krematos.dto.ApiError;
import krematos.exception.ErrorCode;
import krematos.exception.email.EmailAlreadyExistsException;
import krematos.exception.product.FileStorageException;
import krematos.exception.product.InvalidFileException;
import krematos.exception.product.ProductImageFileIsTooBig;
import krematos.exception.product.ProductNotFoundException;
import krematos.exception.token.InvalidTokenException;
import krematos.exception.token.TokenExpiredException;
import krematos.exception.user.UserAlreadyExistException;
import krematos.exception.user.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExistException(
            UserAlreadyExistException ex,
            HttpServletRequest request) {

        log.warn("Uživatel již existuje {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorCode errorCode = ErrorCode.USER_ALREADY_EXISTS;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    /**
     * VALIDACE VSTUPŮ (@Valid) - 400 Bad Request
     * Toto se spustí, když klient pošle neplatná data (např. krátké heslo, špatný
     * email).
     * Místo jedné chyby vrátí mapu všech chyb (pole -> chyba).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        log.warn("Validace selhala na {}: {}", request.getRequestURI(), fieldErrors);

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

        // Combine field errors into a single message
        String message = errorCode.getDefaultMessage() + " " + fieldErrors.toString();

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                message,
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    /**
     * CHYBA FORMÁTU JSON - 400 Bad Request
     * Spustí se, když klient pošle poškozený JSON (např. chybějící čárka,
     * nebo posílá String do pole, které očekává Integer).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonErrors(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Nesprávný JSON formát {}", request.getRequestURI(), ex);

        ErrorCode errorCode = ErrorCode.INVALID_JSON_FORMAT;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getDefaultMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    // 401 - Špatné heslo nebo jméno
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        log.warn("Bad credentials attempt on {}", request.getRequestURI());

        ErrorCode errorCode = ErrorCode.BAD_CREDENTIALS;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getDefaultMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    /**
     * ✅ Řeší chyby @PreAuthorize (chybějící role)
     * Vrací 403 Forbidden místo 500.
     */
    @ExceptionHandler({ AccessDeniedException.class, AuthorizationDeniedException.class })
    public ResponseEntity<ApiError> handleAccessDeniedException(
            Exception ex,
            HttpServletRequest request) {

        log.warn("Přístup odepřen {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getDefaultMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    /**
     * 3. DUPLICITNÍ DATA V DB - 409 Conflict
     * Spustí se, když se snažíte uložit entitu s unikátním polem, které už
     * existuje.
     * (Např. registrace emailu, který už v DB je, a validace v Service vrstvě to
     * nezachytila).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDatabaseConflict(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.error("Data integrity violation on {}", request.getRequestURI(), ex);

        ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getDefaultMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("Email již existuje {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorCode errorCode = ErrorCode.EMAIL_ALREADY_EXISTS;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    // 415 - Nepodporovaný formát dat
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        log.warn("Nepodporovaný formát dat {}: {}", request.getRequestURI(), ex.getContentType());

        ErrorCode errorCode = ErrorCode.UNSUPPORTED_MEDIA_TYPE;

        String message = errorCode.getDefaultMessage() + ": " + ex.getContentType();

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                message,
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    // 423 - Účet uzamčen
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiError> handleLocked(
            AccountLockedException ex,
            HttpServletRequest request) {

        log.warn("Účet uzamčen {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorCode errorCode = ErrorCode.ACCOUNT_LOCKED;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    // 404 - Nenalezeno (např. pro admina)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {

        log.warn("User not found on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    // Řeší expiraci tokenu -> 400
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiError> handleTokenExpired(
            TokenExpiredException ex,
            HttpServletRequest request) {

        log.warn("Token expired on {}", request.getRequestURI());

        ErrorCode errorCode = ErrorCode.TOKEN_EXPIRED;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getDefaultMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    // Řeší neplatný token -> 400 (nebo 404)
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiError> handleInvalidToken(
            InvalidTokenException ex,
            HttpServletRequest request) {

        log.warn("Invalid token on {}", request.getRequestURI());

        ErrorCode errorCode = ErrorCode.INVALID_TOKEN;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getDefaultMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    // Obecná výjimka pro ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        log.warn("Response status exception on {}: {}", request.getRequestURI(), ex.getReason());

        ApiError apiError = new ApiError(
                ex.getStatusCode().value(),
                "RESPONSE_STATUS_EXCEPTION",
                ex.getReason() != null ? ex.getReason() : "Chyba při zpracování požadavku",
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(apiError);
    }

    /**
     * ✅ Řeší IllegalArgumentException (např. duplicitní email, neplatné argumenty)
     * Vrací 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Illegal argument on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorCode errorCode = ErrorCode.ILLEGAL_ARGUMENT;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    /**
     * ✅ Řeší situaci, kdy endpoint neexistuje (404).
     * Zabrání tomu, aby to spadlo do obecné 500 chyby.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found on {}: {}", request.getRequestURI(), ex.getResourcePath());

        ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;

        String message = errorCode.getDefaultMessage() + ": " + ex.getResourcePath();

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                message,
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    // 1. Chyba validace souboru -> 400 Bad Request
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiError> handleInvalidFile(
            InvalidFileException ex,
            HttpServletRequest request) {

        log.warn("Invalid file on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_FILE;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    // 2. Chyba IO operace na serveru -> 500 Internal Server Error
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiError> handleFileStorage(
            FileStorageException ex,
            HttpServletRequest request) {

        log.error("File storage error on {}", request.getRequestURI(), ex);

        ErrorCode errorCode = ErrorCode.FILE_STORAGE_ERROR;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getDefaultMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    // 3. Produkt nenalezen -> 404 Not Found
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductNotFound(
            ProductNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Product not found on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorCode errorCode = ErrorCode.PRODUCT_NOT_FOUND;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    @ExceptionHandler(ProductImageFileIsTooBig.class)
    public ResponseEntity<ApiError> handleProductImageFileIsTooBig(
            ProductImageFileIsTooBig ex,
            HttpServletRequest request) {

        log.warn("File too large on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorCode errorCode = ErrorCode.FILE_TOO_LARGE;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error on {}", request.getRequestURI(), ex);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        ApiError apiError = new ApiError(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getDefaultMessage(),
                request.getRequestURI(),
                Instant.now());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiError);
    }

}
