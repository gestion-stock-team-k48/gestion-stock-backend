package cm.kfokam.stock.mvtstk.dto;

import java.math.BigDecimal;

public record AlerteStockResponse(
        Long articleId,
        String code,
        String designation,
        BigDecimal quantiteStock,
        BigDecimal seuilMinimum
) {
}
