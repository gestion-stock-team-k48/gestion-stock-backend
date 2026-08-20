package cm.kfokam.stock.dashboard.dto;

import java.math.BigDecimal;

public record TopArticleVenduResponse(
        Long articleId,
        String designation,
        BigDecimal quantiteVendue
) {
}
