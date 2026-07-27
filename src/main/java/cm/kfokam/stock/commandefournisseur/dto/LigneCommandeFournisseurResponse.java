package cm.kfokam.stock.commandefournisseur.dto;

import java.math.BigDecimal;

public record LigneCommandeFournisseurResponse(
        Long id,
        Long articleId,
        String articleDesignation,
        Integer quantite,
        BigDecimal prixUnitaireHt,
        BigDecimal prixUnitaireTtc
) {
}