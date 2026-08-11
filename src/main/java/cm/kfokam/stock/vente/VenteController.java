package cm.kfokam.stock.vente;

import cm.kfokam.stock.common.dto.PageResponse;
import cm.kfokam.stock.vente.dto.VenteRequest;
import cm.kfokam.stock.vente.dto.VenteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ventes")
@RequiredArgsConstructor
@Tag(name = "Ventes", description = "Gestion des ventes")
public class VenteController {

    private final VenteService venteService;

    @Operation(summary = "Créer une vente", description = "Enregistre une vente et déclenche la sortie de stock associée")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vente créée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Article introuvable"),
            @ApiResponse(responseCode = "409", description = "Code de vente déjà utilisé ou stock insuffisant")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<VenteResponse> create(@Valid @RequestBody VenteRequest request) {
        VenteResponse response = venteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Récupérer une vente", description = "Retourne une vente par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vente trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Vente introuvable")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<VenteResponse> getById(
            @Parameter(description = "Identifiant de la vente", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(venteService.getById(id));
    }

    @Operation(summary = "Récupérer une vente par code", description = "Retourne une vente par son code unique")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vente trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Vente introuvable")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/code/{code}")
    public ResponseEntity<VenteResponse> getByCode(
            @Parameter(description = "Code unique de la vente", example = "VTE-2026-0001") @PathVariable String code) {
        return ResponseEntity.ok(venteService.getByCode(code));
    }

    @Operation(summary = "Lister les ventes", description = "Retourne toutes les ventes de l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des ventes"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<VenteResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(venteService.getAll(pageable)));
    }

    @Operation(summary = "Supprimer une vente", description = "Supprime définitivement une vente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vente supprimée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Vente introuvable"),
            @ApiResponse(responseCode = "409", description = "Suppression interdite : la vente a déjà généré des mouvements de stock")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifiant de la vente", example = "1") @PathVariable Long id) {
        venteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
