package cm.kfokam.stock.commandeclient.dto;

import cm.kfokam.stock.commandeclient.model.EtatCommande;
import jakarta.validation.constraints.NotNull;

public record EtatCommandeRequest(

        @NotNull(message = "Le nouvel état est obligatoire")
        EtatCommande etatCommande
) {
}
