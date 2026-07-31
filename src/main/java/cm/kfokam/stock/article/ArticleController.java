package cm.kfokam.stock.article;

import cm.kfokam.stock.article.dto.ArticleRequest;
import cm.kfokam.stock.article.dto.ArticleResponse;
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
@RequestMapping("/articles")
@RequiredArgsConstructor
@Tag(name = "Articles", description = "Gestion du catalogue d'articles")
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "Créer un article", description = "Ajoute un nouvel article au catalogue de l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Article créé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Catégorie introuvable"),
            @ApiResponse(responseCode = "409", description = "Code article déjà utilisé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ArticleResponse> create(@Valid @RequestBody ArticleRequest request) {
        ArticleResponse response = articleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Récupérer un article", description = "Retourne un article par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Article trouvé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Article introuvable")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getById(
            @Parameter(description = "Identifiant de l'article", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(articleService.getById(id));
    }

    @Operation(summary = "Lister les articles", description = "Retourne tous les articles de l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des articles"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<ArticleResponse>> getAll() {
        return ResponseEntity.ok(articleService.getAll());
    }

    @Operation(summary = "Modifier un article", description = "Met à jour les informations d'un article existant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Article mis à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Article ou catégorie introuvable"),
            @ApiResponse(responseCode = "409", description = "Code article déjà utilisé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponse> update(
            @Parameter(description = "Identifiant de l'article", example = "1") @PathVariable Long id,
            @Valid @RequestBody ArticleRequest request) {
        return ResponseEntity.ok(articleService.update(id, request));
    }

    @Operation(summary = "Uploader la photo d'un article", description = "Enregistre la photo d'un article dans le bucket MinIO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photo mise à jour"),
            @ApiResponse(responseCode = "400", description = "Fichier invalide ou manquant"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Article introuvable"),
            @ApiResponse(responseCode = "500", description = "Échec du stockage du fichier")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArticleResponse> uploadPhoto(
            @Parameter(description = "Identifiant de l'article", example = "1") @PathVariable Long id,
            @Parameter(description = "Fichier image à uploader") @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(articleService.uploadPhoto(id, file));
    }

    @Operation(summary = "Supprimer un article", description = "Supprime définitivement un article du catalogue")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Article supprimé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Article introuvable")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifiant de l'article", example = "1") @PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
