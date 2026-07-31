package cm.kfokam.stock.client;

import cm.kfokam.stock.client.dto.ClientRequest;
import cm.kfokam.stock.client.dto.ClientResponse;
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
@RequestMapping("/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Gestion des clients et de leurs photos")
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Créer un client", description = "Ajoute un nouveau client pour l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client créé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        ClientResponse response = clientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Récupérer un client", description = "Retourne un client par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client trouvé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Client introuvable")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getById(
            @Parameter(description = "Identifiant du client", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    @Operation(summary = "Lister les clients", description = "Retourne tous les clients de l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des clients"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAll() {
        return ResponseEntity.ok(clientService.getAll());
    }

    @Operation(summary = "Modifier un client", description = "Met à jour les informations d'un client existant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client mis à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Client introuvable"),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(
            @Parameter(description = "Identifiant du client", example = "1") @PathVariable Long id,
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @Operation(summary = "Uploader la photo d'un client", description = "Enregistre la photo de profil d'un client sur le stockage de fichiers et remplace l'ancienne le cas échéant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photo mise à jour"),
            @ApiResponse(responseCode = "400", description = "Fichier invalide ou manquant"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Client introuvable"),
            @ApiResponse(responseCode = "500", description = "Échec du stockage du fichier")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClientResponse> uploadPhoto(
            @Parameter(description = "Identifiant du client", example = "1") @PathVariable Long id,
            @Parameter(description = "Fichier image à uploader") @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(clientService.uploadPhoto(id, file));
    }

    @Operation(summary = "Supprimer un client", description = "Supprime définitivement un client")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client supprimé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Client introuvable")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifiant du client", example = "1") @PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
