package cm.kfokam.stock.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String numTel,
        String rue,
        String ville,
        String codePostal,
        String pays,
        String photo,

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
