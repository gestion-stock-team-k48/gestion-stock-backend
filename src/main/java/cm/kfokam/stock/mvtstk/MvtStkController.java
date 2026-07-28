package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.mvtstk.dto.AlerteStockResponse;
import cm.kfokam.stock.mvtstk.dto.MvtStkCorrectionRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/mouvements-stock")
@RequiredArgsConstructor
public class MvtStkController {

    private final MvtStkService mvtStkService;

    @PostMapping("/entree")
    public ResponseEntity<MvtStkResponse> entreeStock(@Valid @RequestBody MvtStkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mvtStkService.entreeStock(request));
    }

    @PostMapping("/sortie")
    public ResponseEntity<MvtStkResponse> sortieStock(@Valid @RequestBody MvtStkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mvtStkService.sortieStock(request));
    }

    @PostMapping("/correction-positive")
    public ResponseEntity<MvtStkResponse> correctionStockPos(@Valid @RequestBody MvtStkCorrectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mvtStkService.correctionStockPos(request));
    }

    @PostMapping("/correction-negative")
    public ResponseEntity<MvtStkResponse> correctionStockNeg(@Valid @RequestBody MvtStkCorrectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mvtStkService.correctionStockNeg(request));
    }

    @GetMapping("/article/{idArticle}")
    public ResponseEntity<List<MvtStkResponse>> mvtStkArticle(@PathVariable Long idArticle) {
        return ResponseEntity.ok(mvtStkService.mvtStkArticle(idArticle));
    }

    @GetMapping("/article/{idArticle}/stock-reel")
    public ResponseEntity<BigDecimal> stockReelArticle(@PathVariable Long idArticle) {
        return ResponseEntity.ok(mvtStkService.stockReelArticle(idArticle));
    }

    @GetMapping("/alertes-stock")
    public ResponseEntity<List<AlerteStockResponse>> alertesStock() {
        return ResponseEntity.ok(mvtStkService.articlesEnAlerte());
    }
}