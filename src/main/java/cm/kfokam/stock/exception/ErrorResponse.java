package cm.kfokam.stock.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Format d'erreur unique renvoyé par {@link GlobalExceptionHandler} pour toute réponse en échec,
 * conçu pour être facilement typé côté frontend (Angular {@code HttpErrorResponse.error} /
 * React Axios {@code error.response.data}) :
 * <pre>{@code
 * interface ErrorResponse {
 *   timestamp: string;       // ISO-8601, ex: "2026-08-11T10:39:00"
 *   status: number;          // 400, 401, 403, 404, 409, 500 ...
 *   error: string;           // libellé HTTP standard, ex: "Bad Request"
 *   message: string;         // message lisible, affichable tel quel
 *   path: string;            // URI de la requête en échec
 *   validationErrors?: Record<string, string>; // uniquement en cas d'échec de validation @Valid
 * }
 * }</pre>
 * {@code timestamp} est un {@link LocalDateTime}, sérialisé en chaîne ISO-8601 par la
 * configuration Jackson globale ({@code JacksonConfig}).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }
}
