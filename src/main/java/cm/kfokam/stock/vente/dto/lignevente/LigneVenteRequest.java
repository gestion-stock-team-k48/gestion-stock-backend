package cm.kfokam.stock.vente.dto.lignevente;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LigneVenteRequest(

        @NotNull(message = "L'article est obligatoire")
        Long articleId,

        @NotNull(message = "La quantité est obligatoire")
        @Positive(message = "La quantité doit être positive")
        BigDecimal quantite
) {
}