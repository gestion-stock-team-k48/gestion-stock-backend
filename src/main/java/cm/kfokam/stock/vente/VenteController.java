package cm.kfokam.stock.vente;

import cm.kfokam.stock.vente.dto.VenteRequest;
import cm.kfokam.stock.vente.dto.VenteResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
public class VenteController {

    private final VenteService venteService;

    @PostMapping
    public ResponseEntity<VenteResponse> create(@Valid @RequestBody VenteRequest request) {
        VenteResponse response = venteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(venteService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<VenteResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(venteService.getByCode(code));
    }

    @GetMapping
    public ResponseEntity<List<VenteResponse>> getAll() {
        return ResponseEntity.ok(venteService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        venteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}