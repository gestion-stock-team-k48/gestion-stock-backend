package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.fournisseur.dto.FournisseurRequest;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
@RequiredArgsConstructor
@Tag(name = "Fournisseurs", description = "Gestion des fournisseurs")
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @Operation(summary = "Créer un fournisseur", description = "Ajoute un nouveau fournisseur pour l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fournisseur créé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FournisseurResponse> create(@Valid @RequestBody FournisseurRequest request) {
        FournisseurResponse response = fournisseurService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Récupérer un fournisseur", description = "Retourne un fournisseur par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fournisseur trouvé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Fournisseur introuvable")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<FournisseurResponse> getById(
            @Parameter(description = "Identifiant du fournisseur", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(fournisseurService.getById(id));
    }

    @Operation(summary = "Lister les fournisseurs", description = "Retourne tous les fournisseurs de l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des fournisseurs"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<FournisseurResponse>> getAll() {
        return ResponseEntity.ok(fournisseurService.getAll());
    }

    @Operation(summary = "Modifier un fournisseur", description = "Met à jour les informations d'un fournisseur existant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fournisseur mis à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Fournisseur introuvable"),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FournisseurResponse> update(
            @Parameter(description = "Identifiant du fournisseur", example = "1") @PathVariable Long id,
            @Valid @RequestBody FournisseurRequest request) {
        return ResponseEntity.ok(fournisseurService.update(id, request));
    }

    @Operation(summary = "Uploader la photo d'un fournisseur", description = "Enregistre la photo d'un fournisseur dans le bucket MinIO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photo mise à jour"),
            @ApiResponse(responseCode = "400", description = "Fichier invalide ou manquant"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Fournisseur introuvable"),
            @ApiResponse(responseCode = "500", description = "Échec du stockage du fichier")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FournisseurResponse> uploadPhoto(
            @Parameter(description = "Identifiant du fournisseur", example = "1") @PathVariable Long id,
            @Parameter(description = "Fichier image à uploader") @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fournisseurService.uploadPhoto(id, file));
    }

    @Operation(summary = "Supprimer un fournisseur", description = "Supprime définitivement un fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Fournisseur supprimé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Fournisseur introuvable")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifiant du fournisseur", example = "1") @PathVariable Long id) {
        fournisseurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
