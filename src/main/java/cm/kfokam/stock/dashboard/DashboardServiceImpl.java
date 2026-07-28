package cm.kfokam.stock.dashboard;

import cm.kfokam.stock.commandeclient.CommandeClientService;
import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandefournisseur.CommandeFournisseurService;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;
import cm.kfokam.stock.dashboard.dto.DashboardStatsResponse;
import cm.kfokam.stock.dashboard.dto.TopArticleVenduResponse;
import cm.kfokam.stock.vente.VenteService;
import cm.kfokam.stock.vente.dto.VenteResponse;
import cm.kfokam.stock.vente.dto.ligneVente.LigneVenteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class DashboardServiceImpl implements DashboardService {

    private static final int TOP_ARTICLES_LIMIT = 5;
    private static final Set<String> ETATS_EN_COURS = Set.of("EN_PREPARATION", "VALIDEE");
    private static final String ETAT_LIVREE = "LIVREE";

    private final VenteService venteService;
    private final CommandeClientService commandeClientService;
    private final CommandeFournisseurService commandeFournisseurService;

    @Override
    public DashboardStatsResponse getStatistiques() {
        List<VenteResponse> ventes = venteService.getAll();
        List<CommandeClientResponse> commandesClient = commandeClientService.getAll();
        List<CommandeFournisseurResponse> commandesFournisseur = commandeFournisseurService.getAll();

        YearMonth moisCourant = YearMonth.now();

        return new DashboardStatsResponse(
                chiffreAffaires(ventes, vente -> true),
                chiffreAffaires(ventes, vente -> appartientAuMois(vente, moisCourant)),
                compter(commandesClient, CommandeClientResponse::etatCommande, ETATS_EN_COURS::contains),
                compter(commandesClient, CommandeClientResponse::etatCommande, ETAT_LIVREE::equals),
                compter(commandesFournisseur, CommandeFournisseurResponse::etatCommande, ETATS_EN_COURS::contains),
                compter(commandesFournisseur, CommandeFournisseurResponse::etatCommande, ETAT_LIVREE::equals),
                topArticlesVendus(ventes)
        );
    }

    private boolean appartientAuMois(VenteResponse vente, YearMonth moisCourant) {
        return YearMonth.from(vente.dateVente().atZone(ZoneId.systemDefault())).equals(moisCourant);
    }

    private BigDecimal chiffreAffaires(List<VenteResponse> ventes, Predicate<VenteResponse> filtre) {
        return ventes.stream()
                .filter(filtre)
                .flatMap(vente -> vente.lignes().stream())
                .map(ligne -> ligne.prixUnitaire().multiply(ligne.quantite()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private <T> long compter(List<T> commandes, Function<T, ? extends Enum<?>> etatExtractor, Predicate<String> etatFiltre) {
        return commandes.stream()
                .filter(commande -> etatFiltre.test(etatExtractor.apply(commande).name()))
                .count();
    }

    private List<TopArticleVenduResponse> topArticlesVendus(List<VenteResponse> ventes) {
        List<LigneVenteResponse> lignes = ventes.stream().flatMap(vente -> vente.lignes().stream()).toList();

        Map<Long, String> designations = lignes.stream()
                .collect(Collectors.toMap(LigneVenteResponse::articleId, LigneVenteResponse::articleDesignation, (a, b) -> a));

        Map<Long, BigDecimal> quantitesParArticle = lignes.stream()
                .collect(Collectors.groupingBy(LigneVenteResponse::articleId,
                        Collectors.reducing(BigDecimal.ZERO, LigneVenteResponse::quantite, BigDecimal::add)));

        return quantitesParArticle.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(TOP_ARTICLES_LIMIT)
                .map(entry -> new TopArticleVenduResponse(entry.getKey(), designations.get(entry.getKey()), entry.getValue()))
                .toList();
    }
}
