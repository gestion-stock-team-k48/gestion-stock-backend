package cm.kfokam.stock.utilisateur.dto;

import cm.kfokam.stock.utilisateur.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record UtilisateurResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        LocalDate dateDeNaissance,
        String photo,
        String rue,
        String ville,
        String codePostal,
        String pays,
        Long entrepriseId,
        String entrepriseNom,
        Set<Role> roles,
        boolean mustChangePassword,

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
