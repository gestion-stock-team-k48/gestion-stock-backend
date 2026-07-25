package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.fournisseur.dto.FournisseurRequest;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @PostMapping
    public ResponseEntity<FournisseurResponse> create(@Valid @RequestBody FournisseurRequest request) {
        FournisseurResponse response = fournisseurService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FournisseurResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fournisseurService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<FournisseurResponse>> getAll() {
        return ResponseEntity.ok(fournisseurService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FournisseurResponse> update(@PathVariable Long id, @Valid @RequestBody FournisseurRequest request) {
        return ResponseEntity.ok(fournisseurService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fournisseurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}