package cm.kfokam.stock.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatsResponse(
        BigDecimal chiffreAffairesTotal,
        BigDecimal chiffreAffairesMoisCourant,
        long commandesClientEnCours,
        long commandesClientLivrees,
        long commandesFournisseurEnCours,
        long commandesFournisseurLivrees,
        List<TopArticleVenduResponse> topArticlesVendus
) {
}
