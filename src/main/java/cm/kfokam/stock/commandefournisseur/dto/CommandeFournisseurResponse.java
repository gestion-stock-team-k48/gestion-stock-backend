package cm.kfokam.stock.commandefournisseur.dto;

import cm.kfokam.stock.commandefournisseur.model.EtatCommande;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        List<LigneCommandeFournisseurResponse> lignes
) {
}