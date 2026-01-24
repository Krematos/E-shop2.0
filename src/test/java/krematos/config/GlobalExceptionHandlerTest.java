package krematos.config;

import jakarta.servlet.http.HttpServletRequest;
import krematos.dto.ApiError;
import krematos.exception.email.EmailAlreadyExistsException;
import krematos.exception.product.FileStorageException;
import krematos.exception.product.InvalidFileException;
import krematos.exception.product.ProductImageFileIsTooBig;
import krematos.exception.product.ProductNotFoundException;
import krematos.exception.token.InvalidTokenException;
import krematos.exception.token.TokenExpiredException;
import krematos.exception.user.UserAlreadyExistException;
import krematos.exception.user.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.security.auth.login.AccountLockedException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit test pro GlobalExceptionHandler.
 * Testuje všechny exception handlery a ověřuje správnou strukturu ApiError
 * responses.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        when(mockRequest.getRequestURI()).thenReturn("/test/endpoint");
    }

    // ==================== VALIDATION ERRORS ====================

    @Test
    @DisplayName("handleValidationExceptions should return 400 with field errors")
    void handleValidationExceptions_ShouldReturnBadRequest() {
        // Given
        FieldError fieldError1 = new FieldError("user", "email", "Email je povinný");
        FieldError fieldError2 = new FieldError("user", "password", "Heslo musí mít alespoň 8 znaků");

        BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleValidationExceptions(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().errorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).contains("Neplatná vstupní data");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleJsonErrors should return 400 for invalid JSON")
    void handleJsonErrors_ShouldReturnBadRequest() {
        // Given
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Invalid JSON");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleJsonErrors(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().errorCode()).isEqualTo("INVALID_JSON_FORMAT");
        assertThat(response.getBody().message()).contains("Neplatný formát JSON");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleIllegalArgumentException should return 400")
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Neplatný argument");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleIllegalArgumentException(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().errorCode()).isEqualTo("ILLEGAL_ARGUMENT");
        assertThat(response.getBody().message()).isEqualTo("Neplatný argument");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    // ==================== AUTHENTICATION & AUTHORIZATION ====================

    @Test
    @DisplayName("handleBadCredentials should return 401 for invalid credentials")
    void handleBadCredentials_ShouldReturnUnauthorized() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleBadCredentials(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().errorCode()).isEqualTo("BAD_CREDENTIALS");
        assertThat(response.getBody().message()).contains("Neplatné přihlašovací údaje");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleAccessDeniedException should return 403 for AccessDeniedException")
    void handleAccessDeniedException_ShouldReturnForbidden() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleAccessDeniedException(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().errorCode()).isEqualTo("ACCESS_DENIED");
        assertThat(response.getBody().message()).contains("Přístup odepřen");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleAccessDeniedException should return 403 for AuthorizationDeniedException")
    void handleAuthorizationDeniedException_ShouldReturnForbidden() {
        // Given
        AuthorizationDeniedException exception = new AuthorizationDeniedException("Authorization denied");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleAccessDeniedException(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().errorCode()).isEqualTo("ACCESS_DENIED");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    // ==================== NOT FOUND ERRORS ====================

    @Test
    @DisplayName("handleUserNotFound should return 404")
    void handleUserNotFound_ShouldReturnNotFound() {
        // Given
        UserNotFoundException exception = new UserNotFoundException("Uživatel s ID 123 nebyl nalezen");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleUserNotFound(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().errorCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(response.getBody().message()).contains("Uživatel s ID 123 nebyl nalezen");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleProductNotFound should return 404")
    void handleProductNotFound_ShouldReturnNotFound() {
        // Given
        ProductNotFoundException exception = new ProductNotFoundException("Produkt s ID 456 nebyl nalezen");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleProductNotFound(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().errorCode()).isEqualTo("PRODUCT_NOT_FOUND");
        assertThat(response.getBody().message()).contains("Produkt s ID 456 nebyl nalezen");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleNoResourceFound should return 404 for missing endpoints")
    void handleNoResourceFound_ShouldReturnNotFound() {
        // Given
        NoResourceFoundException exception = new NoResourceFoundException(null, "/api/nonexistent");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleNoResourceFound(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().errorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().message()).contains("Endpoint nebo zdroj nenalezen");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    // ==================== CONFLICT ERRORS ====================

    @Test
    @DisplayName("handleUserAlreadyExistException should return 409")
    void handleUserAlreadyExists_ShouldReturnConflict() {
        // Given
        UserAlreadyExistException exception = new UserAlreadyExistException("Uživatel již existuje");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleUserAlreadyExistException(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().errorCode()).isEqualTo("USER_ALREADY_EXISTS");
        assertThat(response.getBody().message()).isEqualTo("Uživatel již existuje");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleEmailAlreadyExistsException should return 409")
    void handleEmailAlreadyExists_ShouldReturnConflict() {
        // Given
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("Email již existuje");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleEmailAlreadyExistsException(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().errorCode()).isEqualTo("EMAIL_ALREADY_EXISTS");
        assertThat(response.getBody().message()).isEqualTo("Email již existuje");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleDatabaseConflict should return 409 for data integrity violations")
    void handleDatabaseConflict_ShouldReturnConflict() {
        // Given
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Duplicate key");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleDatabaseConflict(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().errorCode()).isEqualTo("DATA_INTEGRITY_VIOLATION");
        assertThat(response.getBody().message()).contains("konfliktu dat");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    // ==================== FILE HANDLING ERRORS ====================

    @Test
    @DisplayName("handleInvalidFile should return 400 for invalid files")
    void handleInvalidFile_ShouldReturnBadRequest() {
        // Given
        InvalidFileException exception = new InvalidFileException("Soubor není obrázek");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleInvalidFile(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().errorCode()).isEqualTo("INVALID_FILE");
        assertThat(response.getBody().message()).isEqualTo("Soubor není obrázek");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleFileStorage should return 500 for storage errors")
    void handleFileStorage_ShouldReturnInternalServerError() {
        // Given
        FileStorageException exception = new FileStorageException("Nelze uložit soubor");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleFileStorage(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().errorCode()).isEqualTo("FILE_STORAGE_ERROR");
        assertThat(response.getBody().message()).contains("Chyba při zpracování souboru");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleProductImageFileIsTooBig should return 413 for large files")
    void handleProductImageFileIsTooBig_ShouldReturnPayloadTooLarge() {
        // Given
        ProductImageFileIsTooBig exception = new ProductImageFileIsTooBig("Soubor je příliš velký");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleProductImageFileIsTooBig(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(413);
        assertThat(response.getBody().errorCode()).isEqualTo("FILE_TOO_LARGE");
        assertThat(response.getBody().message()).isEqualTo("Soubor je příliš velký");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    // ==================== TOKEN ERRORS ====================

    @Test
    @DisplayName("handleTokenExpired should return 400 for expired tokens")
    void handleTokenExpired_ShouldReturnBadRequest() {
        // Given
        TokenExpiredException exception = new TokenExpiredException("Token vypršel");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleTokenExpired(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().errorCode()).isEqualTo("TOKEN_EXPIRED");
        assertThat(response.getBody().message()).contains("vypršel");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleInvalidToken should return 400 for invalid tokens")
    void handleInvalidToken_ShouldReturnBadRequest() {
        // Given
        InvalidTokenException exception = new InvalidTokenException("Neplatný token");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleInvalidToken(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().errorCode()).isEqualTo("INVALID_TOKEN");
        assertThat(response.getBody().message()).contains("Neplatný odkaz");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    // ==================== OTHER ERRORS ====================

    @Test
    @DisplayName("handleHttpMediaTypeNotSupported should return 415")
    void handleHttpMediaTypeNotSupported_ShouldReturnUnsupportedMediaType() {
        // Given
        HttpMediaTypeNotSupportedException exception = new HttpMediaTypeNotSupportedException("Unsupported media type");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleHttpMediaTypeNotSupported(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(415);
        assertThat(response.getBody().errorCode()).isEqualTo("UNSUPPORTED_MEDIA_TYPE");
        assertThat(response.getBody().message()).contains("Nepodporovaný formát dat");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleLocked should return 423 for locked accounts")
    void handleAccountLocked_ShouldReturnLocked() {
        // Given
        AccountLockedException exception = new AccountLockedException("Účet je uzamčen");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleLocked(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(423);
        assertThat(response.getBody().errorCode()).isEqualTo("ACCOUNT_LOCKED");
        assertThat(response.getBody().message()).isEqualTo("Účet je uzamčen");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleResponseStatusException should return correct status from exception")
    void handleResponseStatusException_ShouldReturnCorrectStatus() {
        // Given
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "Funkce není implementována");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleResponseStatusException(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(501);
        assertThat(response.getBody().errorCode()).isEqualTo("RESPONSE_STATUS_EXCEPTION");
        assertThat(response.getBody().message()).isEqualTo("Funkce není implementována");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
    }

    @Test
    @DisplayName("handleGlobalException should return 500 for unexpected errors")
    void handleGlobalException_ShouldReturnInternalServerError() {
        // Given
        Exception exception = new RuntimeException("Neočekávaná chyba");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleGlobalException(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().errorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().message()).contains("Došlo k neočekávané chybě");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
