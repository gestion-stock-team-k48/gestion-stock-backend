package cm.kfokam.stock.mvtstk.dto;

import cm.kfokam.stock.mvtstk.model.SourceMvtStk;
import cm.kfokam.stock.mvtstk.model.TypeMvtStk;

import java.math.BigDecimal;
import java.time.Instant;

public record MvtStkResponse(
        Long id,
        Instant dateMvt,
        BigDecimal quantite,
        Long articleId,
        String articleDesignation,
        TypeMvtStk typeMvt,
        SourceMvtStk sourceMvt,
        Long idEntreprise
) {
}