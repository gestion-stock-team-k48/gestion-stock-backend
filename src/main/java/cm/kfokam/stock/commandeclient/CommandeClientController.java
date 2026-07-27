package cm.kfokam.stock.commandeclient;

import cm.kfokam.stock.commandeclient.dto.CommandeClientRequest;
import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandeclient.dto.EtatCommandeRequest;
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
@RequestMapping("/api/commandes-client")
@RequiredArgsConstructor
public class CommandeClientController {

    private final CommandeClientService commandeClientService;

    @PostMapping
    public ResponseEntity<CommandeClientResponse> create(@Valid @RequestBody CommandeClientRequest request) {
        CommandeClientResponse response = commandeClientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeClientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(commandeClientService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CommandeClientResponse>> getAll() {
        return ResponseEntity.ok(commandeClientService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommandeClientResponse> update(@PathVariable Long id, @Valid @RequestBody CommandeClientRequest request) {
        return ResponseEntity.ok(commandeClientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandeClientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/etat")
    public ResponseEntity<CommandeClientResponse> updateEtat(@PathVariable Long id, @Valid @RequestBody EtatCommandeRequest request) {
        return ResponseEntity.ok(commandeClientService.updateEtatCommande(id, request.etatCommande()));
    }
}