package cm.kfokam.stock.commandeclient.dto;

import cm.kfokam.stock.commandeclient.model.EtatCommande;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CommandeClientResponse(
        Long id,
        String codeCommande,
        LocalDate dateCommande,
        EtatCommande etatCommande,
        Long idClient,
        String clientNom,
        String clientPrenom,
        BigDecimal totalHt,
        BigDecimal totalTva,
        BigDecimal totalTtc,
        List<LigneCommandeClientResponse> lignes
) {
}
