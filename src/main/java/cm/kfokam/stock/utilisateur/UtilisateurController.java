package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.utilisateur.dto.ChangePasswordRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurResponse;
import cm.kfokam.stock.utilisateur.model.Utilisateur;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs de l'entreprise courante")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @Operation(summary = "Créer un utilisateur", description = "Ajoute un nouvel utilisateur pour l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur créé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    @PostMapping
    public ResponseEntity<UtilisateurResponse> create(@Valid @RequestBody UtilisateurRequest request) {
        UtilisateurResponse response = utilisateurService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Récupérer un utilisateur", description = "Retourne un utilisateur par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> getById(
            @Parameter(description = "Identifiant de l'utilisateur", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getById(id));
    }

    @Operation(summary = "Lister les utilisateurs", description = "Retourne tous les utilisateurs de l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis")
    })
    @GetMapping
    public ResponseEntity<List<UtilisateurResponse>> getAll() {
        return ResponseEntity.ok(utilisateurService.getAll());
    }

    @Operation(summary = "Modifier un utilisateur", description = "Met à jour les informations d'un utilisateur existant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable"),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> update(
            @Parameter(description = "Identifiant de l'utilisateur", example = "1") @PathVariable Long id,
            @Valid @RequestBody UtilisateurRequest request) {
        return ResponseEntity.ok(utilisateurService.update(id, request));
    }

    @Operation(summary = "Uploader la photo d'un utilisateur", description = "Enregistre la photo de l'utilisateur authentifié dans le bucket MinIO (uniquement sa propre photo)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photo mise à jour"),
            @ApiResponse(responseCode = "400", description = "Fichier invalide ou manquant"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - un utilisateur ne peut modifier que sa propre photo"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable"),
            @ApiResponse(responseCode = "500", description = "Échec du stockage du fichier")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UtilisateurResponse> uploadPhoto(
            @Parameter(description = "Identifiant de l'utilisateur", example = "1") @PathVariable Long id,
            @Parameter(description = "Fichier image à uploader") @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(utilisateurService.uploadPhoto(id, file));
    }

    @Operation(summary = "Supprimer un utilisateur", description = "Supprime définitivement un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifiant de l'utilisateur", example = "1") @PathVariable Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Changer son mot de passe", description = "Permet à l'utilisateur authentifié de changer son propre mot de passe")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mot de passe changé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié ou ancien mot de passe incorrect")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Utilisateur utilisateur,
                                                @Valid @RequestBody ChangePasswordRequest request) {
        utilisateurService.changePassword(utilisateur.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
