package cm.kfokam.stock.mvtstk.dto;

import cm.kfokam.stock.mvtstk.model.SourceMvtStk;
import cm.kfokam.stock.mvtstk.model.TypeMvtStk;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record MvtStkResponse(
        Long id,
        Instant dateMvt,
        BigDecimal quantite,
        Long articleId,
        String articleDesignation,
        TypeMvtStk typeMvt,
        SourceMvtStk sourceMvt,
        String motif,
        Long idEntreprise,

        @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Date de création", example = "2026-08-10T11:30:13")
        LocalDateTime createdAt,

        @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Date de dernière modification", example = "2026-08-10T13:36:32")
        LocalDateTime updatedAt,

        @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Identifiant du créateur")
        String createdBy,

        @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Identifiant du dernier modificateur")
        String updatedBy
) {
}
