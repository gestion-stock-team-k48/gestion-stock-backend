package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.mvtstk.dto.AlerteStockResponse;
import cm.kfokam.stock.mvtstk.dto.MvtStkCorrectionRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/mouvements-stock")
@RequiredArgsConstructor
@Tag(name = "Mouvements de stock", description = "Gestion des entrées, sorties, corrections et alertes de stock")
public class MvtStkController {

    private final MvtStkService mvtStkService;

    @Operation(summary = "Enregistrer une entrée de stock", description = "Ajoute une quantité au stock d'un article")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mouvement enregistré"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Article introuvable")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/entree")
    public ResponseEntity<MvtStkResponse> entreeStock(@Valid @RequestBody MvtStkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mvtStkService.entreeStock(request));
    }

    @Operation(summary = "Enregistrer une sortie de stock", description = "Retire une quantité du stock d'un article")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mouvement enregistré"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Article introuvable"),
            @ApiResponse(responseCode = "409", description = "Stock insuffisant")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sortie")
    public ResponseEntity<MvtStkResponse> sortieStock(@Valid @RequestBody MvtStkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mvtStkService.sortieStock(request));
    }

    @Operation(summary = "Enregistrer une correction positive de stock", description = "Ajuste à la hausse le stock d'un article (ex: inventaire)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mouvement enregistré"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Article introuvable")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/correction-positive")
    public ResponseEntity<MvtStkResponse> correctionStockPos(@Valid @RequestBody MvtStkCorrectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mvtStkService.correctionStockPos(request));
    }

    @Operation(summary = "Enregistrer une correction négative de stock", description = "Ajuste à la baisse le stock d'un article (ex: inventaire, casse)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mouvement enregistré"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Article introuvable"),
            @ApiResponse(responseCode = "409", description = "Stock insuffisant")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/correction-negative")
    public ResponseEntity<MvtStkResponse> correctionStockNeg(@Valid @RequestBody MvtStkCorrectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mvtStkService.correctionStockNeg(request));
    }

    @Operation(summary = "Historique des mouvements d'un article", description = "Retourne tous les mouvements de stock enregistrés pour un article")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historique des mouvements"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Article introuvable")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/article/{idArticle}")
    public ResponseEntity<List<MvtStkResponse>> mvtStkArticle(
            @Parameter(description = "Identifiant de l'article", example = "1") @PathVariable Long idArticle) {
        return ResponseEntity.ok(mvtStkService.mvtStkArticle(idArticle));
    }

    @Operation(summary = "Stock réel d'un article", description = "Calcule la quantité en stock réelle d'un article à partir de ses mouvements")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock réel calculé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Article introuvable")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/article/{idArticle}/stock-reel")
    public ResponseEntity<BigDecimal> stockReelArticle(
            @Parameter(description = "Identifiant de l'article", example = "1") @PathVariable Long idArticle) {
        return ResponseEntity.ok(mvtStkService.stockReelArticle(idArticle));
    }

    @Operation(summary = "Articles en alerte de stock", description = "Retourne les articles dont le stock réel est sous le seuil minimum")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des articles en alerte"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/alertes-stock")
    public ResponseEntity<List<AlerteStockResponse>> alertesStock() {
        return ResponseEntity.ok(mvtStkService.articlesEnAlerte());
    }
}
