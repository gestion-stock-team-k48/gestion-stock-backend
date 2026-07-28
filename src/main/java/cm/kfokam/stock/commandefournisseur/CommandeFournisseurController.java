package cm.kfokam.stock.commandefournisseur;

import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurRequest;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;
import cm.kfokam.stock.commandefournisseur.dto.EtatCommandeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class CommandeFournisseurController {

    private final CommandeFournisseurService commandeFournisseurService;

    @PostMapping
    public ResponseEntity<CommandeFournisseurResponse> create(@Valid @RequestBody CommandeFournisseurRequest request) {
        CommandeFournisseurResponse response = commandeFournisseurService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeFournisseurResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(commandeFournisseurService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CommandeFournisseurResponse>> getAll() {
        return ResponseEntity.ok(commandeFournisseurService.getAll());
    }

    @GetMapping("/fournisseur/{idFournisseur}")
    public ResponseEntity<List<CommandeFournisseurResponse>> getHistoriqueByFournisseur(@PathVariable Long idFournisseur) {
        return ResponseEntity.ok(commandeFournisseurService.getHistoriqueByFournisseur(idFournisseur));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommandeFournisseurResponse> update(@PathVariable Long id, @Valid @RequestBody CommandeFournisseurRequest request) {
        return ResponseEntity.ok(commandeFournisseurService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandeFournisseurService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/etat")
    public ResponseEntity<CommandeFournisseurResponse> updateEtat(@PathVariable Long id, @Valid @RequestBody EtatCommandeRequest request) {
        return ResponseEntity.ok(commandeFournisseurService.updateEtatCommande(id, request.etatCommande()));
    }
}