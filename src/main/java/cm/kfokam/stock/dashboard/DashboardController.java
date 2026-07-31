package cm.kfokam.stock.dashboard;

import cm.kfokam.stock.dashboard.dto.DashboardStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Tableau de bord", description = "Statistiques globales de l'entreprise courante")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Statistiques du tableau de bord", description = "Retourne les indicateurs clés (ventes, stock, commandes) de l'entreprise courante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistiques calculées"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/statistiques")
    public ResponseEntity<DashboardStatsResponse> statistiques() {
        return ResponseEntity.ok(dashboardService.getStatistiques());
    }
}
