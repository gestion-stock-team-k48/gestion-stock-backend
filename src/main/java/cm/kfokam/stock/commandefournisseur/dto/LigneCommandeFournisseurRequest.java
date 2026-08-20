package cm.kfokam.stock.commandefournisseur.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LigneCommandeFournisseurRequest(

        @NotNull(message = "L'article est obligatoire")
        Long articleId,

        @NotNull(message = "La quantité est obligatoire")
        @Positive(message = "La quantité doit être positive")
        Integer quantite
) {
}