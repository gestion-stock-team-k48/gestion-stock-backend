package cm.kfokam.stock.vente.dto;

import cm.kfokam.stock.vente.dto.lignevente.LigneVenteRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record VenteRequest(

        @Size(max = 30, message = "Le code ne doit pas dépasser 30 caractères")
        String code,

        @Schema(description = "Date de la vente ; l'instant courant si absente. Ne peut pas être dans le futur.",
                example = "2026-05-14T09:20:00Z")
        @PastOrPresent(message = "La date de vente ne peut pas être dans le futur")
        Instant dateVente,

        @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
        String commentaire,

        @NotEmpty(message = "La vente doit contenir au moins une ligne")
        @Valid
        List<LigneVenteRequest> lignes
) {
}
