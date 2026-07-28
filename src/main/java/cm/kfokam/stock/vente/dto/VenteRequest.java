package cm.kfokam.stock.vente.dto;

import cm.kfokam.stock.vente.dto.ligneVente.LigneVenteRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VenteRequest(

        @Size(max = 30, message = "Le code ne doit pas dépasser 30 caractères")
        String code,

        @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
        String commentaire,

        @NotEmpty(message = "La vente doit contenir au moins une ligne")
        @Valid
        List<LigneVenteRequest> lignes
) {
}