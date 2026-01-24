package krematos.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Došlo k neočekávané chybě."),

    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "Neplatná vstupní data."),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Uživatel nebyl nalezen."),

    USER_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "Uživatel již existuje."),

    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "Email již existuje."),

    INVALID_JSON_FORMAT(
            HttpStatus.BAD_REQUEST,
            "Neplatný formát JSON požadavku. Zkontrolujte syntaxi a datové typy."),

    BAD_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "Neplatné přihlašovací údaje."),

    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "Přístup odepřen: Nemáte dostatečná oprávnění."),

    DATA_INTEGRITY_VIOLATION(
            HttpStatus.CONFLICT,
            "Operaci nelze provést z důvodu konfliktu dat (např. duplicitní záznam)."),

    UNSUPPORTED_MEDIA_TYPE(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Nepodporovaný formát dat."),

    ACCOUNT_LOCKED(
            HttpStatus.LOCKED,
            "Účet je uzamčen."),

    TOKEN_EXPIRED(
            HttpStatus.BAD_REQUEST,
            "Odkaz pro obnovu hesla již vypršel. Vyžádejte si prosím nový."),

    INVALID_TOKEN(
            HttpStatus.BAD_REQUEST,
            "Neplatný odkaz pro obnovu hesla."),

    ILLEGAL_ARGUMENT(
            HttpStatus.BAD_REQUEST,
            "Neplatný argument."),

    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Endpoint nebo zdroj nenalezen."),

    INVALID_FILE(
            HttpStatus.BAD_REQUEST,
            "Neplatný soubor."),

    FILE_STORAGE_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Chyba při zpracování souboru na serveru."),

    PRODUCT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Produkt nebyl nalezen."),

    FILE_TOO_LARGE(
            HttpStatus.PAYLOAD_TOO_LARGE,
            "Soubor je příliš velký.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
