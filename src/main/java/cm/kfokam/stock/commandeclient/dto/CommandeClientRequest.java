package cm.kfokam.stock.commandeclient.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CommandeClientRequest(

        @Size(max = 30, message = "Le code commande ne doit pas dépasser 30 caractères")
        String codeCommande,

        @NotNull(message = "La date de commande est obligatoire")
        LocalDate dateCommande,

        @NotNull(message = "Le client est obligatoire")
        Long idClient,

        @NotEmpty(message = "La commande doit contenir au moins une ligne")
        @Valid
        List<LigneCommandeClientRequest> lignes
) {
}
