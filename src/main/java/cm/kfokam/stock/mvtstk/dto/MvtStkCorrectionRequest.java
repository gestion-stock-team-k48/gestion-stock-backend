package cm.kfokam.stock.mvtstk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MvtStkCorrectionRequest(

        @NotNull(message = "L'article est obligatoire")
        Long articleId,

        @NotNull(message = "La quantité est obligatoire")
        @Positive(message = "La quantité doit être positive")
        BigDecimal quantite,

        @NotBlank(message = "Le motif est obligatoire")
        @Size(max = 255, message = "Le motif ne doit pas dépasser 255 caractères")
        String motif
) {
}
