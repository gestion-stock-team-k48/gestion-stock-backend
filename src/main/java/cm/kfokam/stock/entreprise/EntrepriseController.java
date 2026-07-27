package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
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
@RequestMapping("/api/entreprises")
@RequiredArgsConstructor
public class EntrepriseController {

    private final EntrepriseService entrepriseService;

    @PostMapping
    public ResponseEntity<EntrepriseResponse> create(@Valid @RequestBody EntrepriseRequest request) {
        EntrepriseResponse response = entrepriseService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntrepriseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(entrepriseService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<EntrepriseResponse>> getAll() {
        return ResponseEntity.ok(entrepriseService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntrepriseResponse> update(@PathVariable Long id, @Valid @RequestBody EntrepriseRequest request) {
        return ResponseEntity.ok(entrepriseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        entrepriseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
