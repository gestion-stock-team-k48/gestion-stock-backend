package cm.kfokam.stock.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpServletRequest requestWithMethod(String method) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn("/articles/1");
        return request;
    }

    @Test
    void handleInvalidOperation_shouldReturn409WithMessage() {
        InvalidOperationException ex = new InvalidOperationException(
                "Impossible de supprimer cette catégorie car elle contient des articles.");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidOperation(ex, requestWithMethod("DELETE"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo(
                "Impossible de supprimer cette catégorie car elle contient des articles.");
    }

    @Test
    void handleDataIntegrityViolation_shouldReturn409WithGenericMessage() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint violation");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, requestWithMethod("DELETE"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo(
                "Impossible d'effectuer cette opération car la ressource est référencée par d'autres données.");
    }

    @Test
    void handleAccessDenied_shouldReturnCustomMessage_whenDeleteRequest() {
        AccessDeniedException ex = new AccessDeniedException("Access Denied");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, requestWithMethod("DELETE"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo(
                "Vous n'avez pas les autorisations nécessaires (Rôle requis) pour exécuter cette suppression.");
    }

    @Test
    void handleAccessDenied_shouldReturnOriginalMessage_whenNotDeleteRequest() {
        AccessDeniedException ex = new AccessDeniedException("Vous ne pouvez modifier que votre propre photo");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, requestWithMethod("POST"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo("Vous ne pouvez modifier que votre propre photo");
    }
}
