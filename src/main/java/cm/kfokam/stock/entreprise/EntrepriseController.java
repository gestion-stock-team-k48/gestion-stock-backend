package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Creation happens exclusively via POST /api/auth/register (tenant self-registration).
// No cross-tenant getAll()/delete() here — there is no platform "super-admin" role to gate them behind.
@RestController
@RequestMapping("/entreprises")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Entreprise", description = "Gestion des informations de l'entreprise courante (tenant)")
public class EntrepriseController {

    private final EntrepriseService entrepriseService;
    private final CurrentUserService currentUserService;

    @Operation(summary = "Récupérer mon entreprise", description = "Retourne les informations de l'entreprise de l'utilisateur authentifié")
    @ApiResponse(responseCode = "200", description = "Entreprise trouvée")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis")
    @ApiResponse(responseCode = "404", description = "Entreprise introuvable")
    @GetMapping("/me")
    public ResponseEntity<EntrepriseResponse> getMine() {
        return ResponseEntity.ok(entrepriseService.getById(currentUserService.getCurrentEntrepriseId()));
    }

    @Operation(summary = "Modifier mon entreprise", description = "Met à jour les informations de l'entreprise de l'utilisateur authentifié")
    @ApiResponse(responseCode = "200", description = "Entreprise mise à jour")
    @ApiResponse(responseCode = "400", description = "Requête invalide")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis")
    @ApiResponse(responseCode = "404", description = "Entreprise introuvable")
    @ApiResponse(responseCode = "409", description = "Code fiscal déjà utilisé")
    @PutMapping("/me")
    public ResponseEntity<EntrepriseResponse> updateMine(@Valid @RequestBody EntrepriseRequest request) {
        return ResponseEntity.ok(entrepriseService.update(currentUserService.getCurrentEntrepriseId(), request));
    }
}
