package cm.kfokam.stock.commandefournisseur.dto;

import cm.kfokam.stock.commandefournisseur.model.EtatCommande;
import jakarta.validation.constraints.NotNull;

public record EtatCommandeRequest(

        @NotNull(message = "Le nouvel état est obligatoire")
        EtatCommande etatCommande
) {
}