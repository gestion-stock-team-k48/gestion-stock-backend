package cm.kfokam.stock.commandeclient.dto;

import java.math.BigDecimal;

public record LigneCommandeClientResponse(
        Long id,
        Long articleId,
        String articleDesignation,
        Integer quantite,
        BigDecimal prixUnitaireHt,
        BigDecimal prixUnitaireTtc
) {
}
