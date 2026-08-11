package cm.kfokam.stock.commandeclient;

import cm.kfokam.stock.commandeclient.dto.CommandeClientRequest;
import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandeclient.dto.EtatCommandeRequest;
import cm.kfokam.stock.common.dto.PageResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/commandes-client")
@RequiredArgsConstructor
@Tag(name = "Commandes Client", description = "Gestion des commandes passées par les clients")
public class CommandeClientController {

    private final CommandeClientService commandeClientService;

    @Operation(summary = "Créer une commande client", description = "Enregistre une nouvelle commande pour un client")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Commande créée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Client ou article introuvable"),
            @ApiResponse(responseCode = "409", description = "Code de commande déjà utilisé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CommandeClientResponse> create(@Valid @RequestBody CommandeClientRequest request) {
        CommandeClientResponse response = commandeClientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Récupérer une commande client", description = "Retourne une commande client par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Commande introuvable")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CommandeClientResponse> getById(
            @Parameter(description = "Identifiant de la commande", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(commandeClientService.getById(id));
    }

    @Operation(summary = "Lister les commandes client", description = "Retourne toutes les commandes client de l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des commandes"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<CommandeClientResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(commandeClientService.getAll(pageable)));
    }

    @Operation(summary = "Historique des commandes d'un client", description = "Retourne toutes les commandes passées par un client donné")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historique des commandes"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Client introuvable")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/client/{idClient}")
    public ResponseEntity<List<CommandeClientResponse>> getHistoriqueByClient(
            @Parameter(description = "Identifiant du client", example = "1") @PathVariable Long idClient) {
        return ResponseEntity.ok(commandeClientService.getHistoriqueByClient(idClient));
    }

    @Operation(summary = "Modifier une commande client", description = "Met à jour les informations d'une commande existante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande mise à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Commande, client ou article introuvable"),
            @ApiResponse(responseCode = "409", description = "Code de commande déjà utilisé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CommandeClientResponse> update(
            @Parameter(description = "Identifiant de la commande", example = "1") @PathVariable Long id,
            @Valid @RequestBody CommandeClientRequest request) {
        return ResponseEntity.ok(commandeClientService.update(id, request));
    }

    @Operation(summary = "Supprimer une commande client", description = "Supprime définitivement une commande client")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Commande supprimée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Commande introuvable"),
            @ApiResponse(responseCode = "409", description = "Suppression interdite : la commande est à l'état LIVREE")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifiant de la commande", example = "1") @PathVariable Long id) {
        commandeClientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Changer l'état d'une commande client", description = "Fait transitionner une commande vers un nouvel état (ex: VALIDEE, LIVREE, ANNULEE)")
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
    public ResponseEntity<CommandeClientResponse> updateEtat(
            @Parameter(description = "Identifiant de la commande", example = "1") @PathVariable Long id,
            @Valid @RequestBody EtatCommandeRequest request) {
        return ResponseEntity.ok(commandeClientService.updateEtatCommande(id, request.etatCommande()));
    }
}
