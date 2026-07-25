package cm.kfokam.stock.article.dto;

import java.math.BigDecimal;

public record ArticleResponse(
        Long id,
        String code,
        String designation,
        BigDecimal prixUnitaireHt,
        BigDecimal tauxTva,
        BigDecimal prixUnitaireTtc,
        String photo,
        Long categoryId,
        String categoryDesignation
) {
}