package cm.kfokam.stock.vente.dto;

import cm.kfokam.stock.vente.dto.lignevente.LigneVenteResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record VenteResponse(
        Long id,
        String code,
        Instant dateVente,
        String commentaire,
        Long idEntreprise,
        List<LigneVenteResponse> lignes,

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
