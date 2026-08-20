package cm.kfokam.stock.commandefournisseur.dto;

import cm.kfokam.stock.commandefournisseur.model.EtatCommande;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CommandeFournisseurResponse(
        Long id,
        String codeCommande,
        LocalDate dateCommande,
        EtatCommande etatCommande,
        Long idFournisseur,
        String fournisseurNom,
        String fournisseurPrenom,
        BigDecimal totalHt,
        BigDecimal totalTva,
        BigDecimal totalTtc,
        List<LigneCommandeFournisseurResponse> lignes,

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
