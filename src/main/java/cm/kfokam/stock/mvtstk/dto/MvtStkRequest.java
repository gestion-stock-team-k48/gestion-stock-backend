package cm.kfokam.stock.mvtstk.dto;

import cm.kfokam.stock.mvtstk.model.SourceMvtStk;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MvtStkRequest(

        @NotNull(message = "L'article est obligatoire")
        Long articleId,

        @NotNull(message = "La quantité est obligatoire")
        @Positive(message = "La quantité doit être positive")
        BigDecimal quantite,

        @NotNull(message = "La source du mouvement est obligatoire")
        SourceMvtStk sourceMvt
) {
}