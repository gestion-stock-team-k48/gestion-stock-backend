package cm.kfokam.stock.vente.dto.ligneVente;

import java.math.BigDecimal;

public record LigneVenteResponse(
        Long id,
        Long articleId,
        String articleDesignation,
        BigDecimal quantite,
        BigDecimal prixUnitaire
) {
}