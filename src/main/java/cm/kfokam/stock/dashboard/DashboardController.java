package cm.kfokam.stock.dashboard;

import cm.kfokam.stock.dashboard.dto.DashboardStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/statistiques")
    public ResponseEntity<DashboardStatsResponse> statistiques() {
        return ResponseEntity.ok(dashboardService.getStatistiques());
    }
}
