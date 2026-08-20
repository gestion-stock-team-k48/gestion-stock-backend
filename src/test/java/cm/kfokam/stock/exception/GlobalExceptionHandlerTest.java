package cm.kfokam.stock.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    @Test
    void handleEmailDelivery_shouldReturn503WithGenericMessage_withoutLeakingCause() {
        EmailDeliveryException ex = new EmailDeliveryException(
                "Échec de l'envoi de l'email 'Réinitialisation de votre mot de passe' à victime@cible.cm",
                new RuntimeException("535 5.7.8 Authentication failed"));

        ResponseEntity<ErrorResponse> response = handler.handleEmailDelivery(ex, requestWithMethod("POST"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().message()).isEqualTo(
                "Le service d'envoi d'emails est momentanément indisponible. Veuillez réessayer plus tard.");
    }

    @Test
    void handleValidation_shouldReturn400WithFieldLevelValidationErrorsMap() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "articleRequest");
        bindingResult.addError(new FieldError("articleRequest", "code", "ne doit pas être vide"));
        bindingResult.addError(new FieldError("articleRequest", "prixVente", "doit être positif"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, requestWithMethod("POST"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().validationErrors())
                .containsEntry("code", "ne doit pas être vide")
                .containsEntry("prixVente", "doit être positif");
    }

    @Test
    @SuppressWarnings("deprecation")
    void handleMessageNotReadable_shouldReturn400_whenBodyIsMalformed() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error");

        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadable(ex, requestWithMethod("POST"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().validationErrors()).isNull();
    }

    @Test
    void handleTypeMismatch_shouldReturn400WithParameterNameAndValue() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", mock(MethodParameter.class), new IllegalArgumentException());

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, requestWithMethod("GET"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("id").contains("abc");
    }
}
