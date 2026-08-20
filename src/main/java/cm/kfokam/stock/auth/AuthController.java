package cm.kfokam.stock.auth;

import cm.kfokam.stock.auth.dto.AuthenticationRequest;
import cm.kfokam.stock.auth.dto.AuthenticationResponse;
import cm.kfokam.stock.auth.dto.ForgotPasswordRequest;
import cm.kfokam.stock.auth.dto.RegisterRequest;
import cm.kfokam.stock.auth.dto.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoints publics (voir SecurityConfig.WHITE_LIST_URLS) : pas de @PreAuthorize ici,
// ils doivent rester accessibles sans authentification préalable.
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Connexion, rafraîchissement de session et inscription des entreprises")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "S'authentifier", description = "Vérifie les identifiants et retourne un token d'accès et un refresh token")
    @ApiResponse(responseCode = "200", description = "Authentification réussie")
    @ApiResponse(responseCode = "400", description = "Requête invalide")
    @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(summary = "Rafraîchir le token", description = "Génère un nouveau token d'accès à partir d'un refresh token valide")
    @ApiResponse(responseCode = "200", description = "Token rafraîchi")
    @ApiResponse(responseCode = "401", description = "Refresh token manquant, invalide ou expiré")
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(summary = "Inscrire une entreprise", description = "Crée une nouvelle entreprise (tenant) avec son premier utilisateur administrateur")
    @ApiResponse(responseCode = "201", description = "Entreprise et utilisateur créés")
    @ApiResponse(responseCode = "400", description = "Requête invalide")
    @ApiResponse(responseCode = "409", description = "Email ou code fiscal déjà utilisé")
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Mot de passe oublié", description = "Envoie un code de réinitialisation par email si le compte existe (la réponse ne révèle jamais si l'email est connu ou non)")
    @ApiResponse(responseCode = "200", description = "Requête traitée")
    @ApiResponse(responseCode = "400", description = "Requête invalide")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Réinitialiser le mot de passe", description = "Définit un nouveau mot de passe à partir d'un code de réinitialisation valide et non expiré")
    @ApiResponse(responseCode = "204", description = "Mot de passe réinitialisé")
    @ApiResponse(responseCode = "400", description = "Requête invalide")
    @ApiResponse(responseCode = "401", description = "Code de réinitialisation invalide ou expiré")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
