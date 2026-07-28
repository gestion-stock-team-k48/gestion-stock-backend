package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Creation happens exclusively via POST /api/auth/register (tenant self-registration).
// No cross-tenant getAll()/delete() here — there is no platform "super-admin" role to gate them behind.
@RestController
@RequestMapping("/api/entreprises")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EntrepriseController {

    private final EntrepriseService entrepriseService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public ResponseEntity<EntrepriseResponse> getMine() {
        return ResponseEntity.ok(entrepriseService.getById(currentUserService.getCurrentEntrepriseId()));
    }

    @PutMapping("/me")
    public ResponseEntity<EntrepriseResponse> updateMine(@Valid @RequestBody EntrepriseRequest request) {
        return ResponseEntity.ok(entrepriseService.update(currentUserService.getCurrentEntrepriseId(), request));
    }
}
