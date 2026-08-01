package cm.kfokam.stock.dashboard;

import cm.kfokam.stock.commandeclient.CommandeClientService;
import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandeclient.model.EtatCommande;
import cm.kfokam.stock.commandefournisseur.CommandeFournisseurService;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;
import cm.kfokam.stock.dashboard.dto.DashboardStatsResponse;
import cm.kfokam.stock.dashboard.dto.TopArticleVenduResponse;
import cm.kfokam.stock.vente.VenteService;
import cm.kfokam.stock.vente.dto.VenteResponse;
import cm.kfokam.stock.vente.dto.ligneVente.LigneVenteResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private VenteService venteService;

    @Mock
    private CommandeClientService commandeClientService;

    @Mock
    private CommandeFournisseurService commandeFournisseurService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getStatistiques_shouldAggregateVentesCommandesAndTopArticles() {
        Instant now = Instant.now();
        Instant moisPasse = now.minus(400, ChronoUnit.DAYS);

        VenteResponse venteRecente = new VenteResponse(1L, "VT-0001", now, null, 1L, List.of(
                new LigneVenteResponse(1L, 10L, "Article 1", new BigDecimal("3"), new BigDecimal("100.00")),
                new LigneVenteResponse(2L, 20L, "Article 2", new BigDecimal("3"), new BigDecimal("20.00"))
        ));
        VenteResponse venteAncienne = new VenteResponse(2L, "VT-0002", moisPasse, null, 1L, List.of(
                new LigneVenteResponse(3L, 10L, "Article 1", new BigDecimal("2"), new BigDecimal("100.00"))
        ));
        when(venteService.getAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(venteRecente, venteAncienne)));

        CommandeClientResponse ccEnPreparation = commandeClient(EtatCommande.EN_PREPARATION);
        CommandeClientResponse ccValidee = commandeClient(EtatCommande.VALIDEE);
        CommandeClientResponse ccLivree = commandeClient(EtatCommande.LIVREE);
        when(commandeClientService.getAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(ccEnPreparation, ccValidee, ccLivree)));

        CommandeFournisseurResponse cfValidee = commandeFournisseur(cm.kfokam.stock.commandefournisseur.model.EtatCommande.VALIDEE);
        CommandeFournisseurResponse cfLivree = commandeFournisseur(cm.kfokam.stock.commandefournisseur.model.EtatCommande.LIVREE);
        when(commandeFournisseurService.getAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(cfValidee, cfLivree)));

        DashboardStatsResponse result = dashboardService.getStatistiques();

        assertThat(result.chiffreAffairesTotal()).isEqualByComparingTo(new BigDecimal("560.00"));
        assertThat(result.chiffreAffairesMoisCourant()).isEqualByComparingTo(new BigDecimal("360.00"));
        assertThat(result.commandesClientEnCours()).isEqualTo(2);
        assertThat(result.commandesClientLivrees()).isEqualTo(1);
        assertThat(result.commandesFournisseurEnCours()).isEqualTo(1);
        assertThat(result.commandesFournisseurLivrees()).isEqualTo(1);
        assertThat(result.topArticlesVendus())
                .extracting(TopArticleVenduResponse::articleId, TopArticleVenduResponse::quantiteVendue)
                .containsExactly(
                        tuple(10L, new BigDecimal("5")),
                        tuple(20L, new BigDecimal("3"))
                );
    }

    @Test
    void getStatistiques_shouldReturnZeroesAndEmptyTop_whenNoVentes() {
        when(venteService.getAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of()));
        when(commandeClientService.getAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of()));
        when(commandeFournisseurService.getAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of()));

        DashboardStatsResponse result = dashboardService.getStatistiques();

        assertThat(result.chiffreAffairesTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.chiffreAffairesMoisCourant()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.topArticlesVendus()).isEmpty();
    }

    private CommandeClientResponse commandeClient(EtatCommande etat) {
        return new CommandeClientResponse(1L, "CC-0001", LocalDate.now(), etat, 1L, "Doe", "John",
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, List.of());
    }

    private CommandeFournisseurResponse commandeFournisseur(cm.kfokam.stock.commandefournisseur.model.EtatCommande etat) {
        return new CommandeFournisseurResponse(1L, "CF-0001", LocalDate.now(), etat, 1L, "Martin", "Paul",
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, List.of());
    }
}
