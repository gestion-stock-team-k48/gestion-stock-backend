package cm.kfokam.stock.commandefournisseur;

import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurRequest;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;
import cm.kfokam.stock.commandefournisseur.dto.EtatCommandeRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/commandes-fournisseur")
@RequiredArgsConstructor
@Tag(name = "Commandes Fournisseur", description = "Gestion des commandes passées aux fournisseurs")
public class CommandeFournisseurController {

    private final CommandeFournisseurService commandeFournisseurService;

    @Operation(summary = "Créer une commande fournisseur", description = "Enregistre une nouvelle commande auprès d'un fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Commande créée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Fournisseur ou article introuvable"),
            @ApiResponse(responseCode = "409", description = "Code de commande déjà utilisé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CommandeFournisseurResponse> create(@Valid @RequestBody CommandeFournisseurRequest request) {
        CommandeFournisseurResponse response = commandeFournisseurService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Récupérer une commande fournisseur", description = "Retourne une commande fournisseur par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Commande introuvable")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CommandeFournisseurResponse> getById(
            @Parameter(description = "Identifiant de la commande", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(commandeFournisseurService.getById(id));
    }

    @Operation(summary = "Lister les commandes fournisseur", description = "Retourne toutes les commandes fournisseur de l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des commandes"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<CommandeFournisseurResponse>> getAll() {
        return ResponseEntity.ok(commandeFournisseurService.getAll());
    }

    @Operation(summary = "Historique des commandes d'un fournisseur", description = "Retourne toutes les commandes passées auprès d'un fournisseur donné")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historique des commandes"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Fournisseur introuvable")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/fournisseur/{idFournisseur}")
    public ResponseEntity<List<CommandeFournisseurResponse>> getHistoriqueByFournisseur(
            @Parameter(description = "Identifiant du fournisseur", example = "1") @PathVariable Long idFournisseur) {
        return ResponseEntity.ok(commandeFournisseurService.getHistoriqueByFournisseur(idFournisseur));
    }

    @Operation(summary = "Modifier une commande fournisseur", description = "Met à jour les informations d'une commande existante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande mise à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Commande, fournisseur ou article introuvable"),
            @ApiResponse(responseCode = "409", description = "Code de commande déjà utilisé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CommandeFournisseurResponse> update(
            @Parameter(description = "Identifiant de la commande", example = "1") @PathVariable Long id,
            @Valid @RequestBody CommandeFournisseurRequest request) {
        return ResponseEntity.ok(commandeFournisseurService.update(id, request));
    }

    @Operation(summary = "Supprimer une commande fournisseur", description = "Supprime définitivement une commande fournisseur")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Commande supprimée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Commande introuvable")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifiant de la commande", example = "1") @PathVariable Long id) {
        commandeFournisseurService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Changer l'état d'une commande fournisseur", description = "Fait transitionner une commande vers un nouvel état (ex: VALIDEE, LIVREE, ANNULEE)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "État de la commande mis à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Commande introuvable"),
            @ApiResponse(responseCode = "409", description = "Transition d'état invalide")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/etat")
    public ResponseEntity<CommandeFournisseurResponse> updateEtat(
            @Parameter(description = "Identifiant de la commande", example = "1") @PathVariable Long id,
            @Valid @RequestBody EtatCommandeRequest request) {
        return ResponseEntity.ok(commandeFournisseurService.updateEtatCommande(id, request.etatCommande()));
    }
}
