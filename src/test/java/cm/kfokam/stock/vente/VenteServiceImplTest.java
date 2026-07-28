package cm.kfokam.stock.vente;

import cm.kfokam.stock.article.ArticleService;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.mvtstk.MvtStkService;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.model.SourceMvtStk;
import cm.kfokam.stock.vente.dto.VenteRequest;
import cm.kfokam.stock.vente.dto.VenteResponse;
import cm.kfokam.stock.vente.dto.ligneVente.LigneVenteRequest;
import cm.kfokam.stock.vente.dto.ligneVente.LigneVenteResponse;
import cm.kfokam.stock.vente.model.LigneVente;
import cm.kfokam.stock.vente.model.Vente;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenteServiceImplTest {

    private static final Long ENTREPRISE_ID = 1L;

    @Mock
    private VenteRepository venteRepository;

    @Mock
    private VenteMapper venteMapper;

    @Mock
    private ArticleService articleService;

    @Mock
    private MvtStkService mvtStkService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private VenteServiceImpl venteService;

    private Article article;
    private ArticleResponse articleResponse;
    private Vente vente;
    private LigneVente ligneVente;
    private VenteRequest request;
    private VenteResponse response;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);

        article = Article.builder().id(1L).code("ART-01").designation("Ordinateur portable").build();
        articleResponse = new ArticleResponse(1L, "ART-01", "Ordinateur portable",
                new BigDecimal("500.00"), new BigDecimal("19.25"), new BigDecimal("596.25"),
                null, new BigDecimal("5"), 1L, "Informatique");

        ligneVente = LigneVente.builder()
                .id(1L)
                .article(article)
                .quantite(new BigDecimal("2"))
                .prixUnitaire(new BigDecimal("596.25"))
                .idEntreprise(1L)
                .build();

        vente = Vente.builder()
                .id(1L)
                .code("VT-2026-0001")
                .dateVente(Instant.parse("2026-07-27T10:00:00Z"))
                .commentaire("Vente comptoir")
                .idEntreprise(1L)
                .lignes(new ArrayList<>(List.of(ligneVente)))
                .build();

        request = new VenteRequest(
                null, "Vente comptoir",
                List.of(new LigneVenteRequest(1L, new BigDecimal("2")))
        );

        response = new VenteResponse(
                1L, "VT-2026-0001", Instant.parse("2026-07-27T10:00:00Z"), "Vente comptoir", 1L,
                List.of(new LigneVenteResponse(1L, 1L, "Ordinateur portable", new BigDecimal("2"), new BigDecimal("596.25")))
        );
    }

    // ------------------------------------------------------------------
    // enregistrerVente (create)
    // ------------------------------------------------------------------

    @Test
    void create_shouldSaveVenteAndLignes_whenValid() {
        when(venteRepository.countByCodeStartingWithAndIdEntreprise(anyString(), eq(ENTREPRISE_ID))).thenReturn(0L);
        when(venteMapper.toEntity(request)).thenReturn(new Vente());
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(venteRepository.save(any(Vente.class))).thenReturn(vente);
        when(venteMapper.toResponse(vente)).thenReturn(response);

        VenteResponse result = venteService.create(request);

        assertThat(result).isEqualTo(response);
        verify(venteRepository).save(any(Vente.class));
    }

    @Test
    void create_shouldVerifyEachArticleExistence_viaArticleService() {
        VenteRequest multiLineRequest = new VenteRequest(
                null, "Vente comptoir",
                List.of(new LigneVenteRequest(1L, new BigDecimal("2")), new LigneVenteRequest(2L, new BigDecimal("1")))
        );
        Article article2 = Article.builder().id(2L).code("ART-02").designation("Souris").build();
        ArticleResponse articleResponse2 = new ArticleResponse(2L, "ART-02", "Souris",
                new BigDecimal("10.00"), new BigDecimal("19.25"), new BigDecimal("11.93"), null, new BigDecimal("5"), 1L, "Informatique");

        when(venteRepository.countByCodeStartingWithAndIdEntreprise(anyString(), eq(ENTREPRISE_ID))).thenReturn(0L);
        when(venteMapper.toEntity(multiLineRequest)).thenReturn(new Vente());
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(articleService.getById(2L)).thenReturn(articleResponse2);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(entityManager.getReference(Article.class, 2L)).thenReturn(article2);
        when(venteRepository.save(any(Vente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(venteMapper.toResponse(any(Vente.class))).thenReturn(response);

        venteService.create(multiLineRequest);

        verify(articleService).getById(1L);
        verify(articleService).getById(2L);
    }

    @Test
    void create_shouldCallSortieStock_forEachLigneVente() {
        when(venteRepository.countByCodeStartingWithAndIdEntreprise(anyString(), eq(ENTREPRISE_ID))).thenReturn(0L);
        when(venteMapper.toEntity(request)).thenReturn(new Vente());
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(venteRepository.save(any(Vente.class))).thenReturn(vente);
        when(venteMapper.toResponse(vente)).thenReturn(response);

        venteService.create(request);

        verify(mvtStkService).sortieStock(argThat((MvtStkRequest mvtRequest) ->
                mvtRequest.articleId().equals(1L)
                        && mvtRequest.quantite().compareTo(new BigDecimal("2")) == 0
                        && mvtRequest.sourceMvt() == SourceMvtStk.VENTE
        ));
    }

    @Test
    void create_shouldCallSortieStock_onceForEachOfMultipleLignes() {
        LigneVente ligne2 = LigneVente.builder()
                .id(2L)
                .article(Article.builder().id(2L).build())
                .quantite(new BigDecimal("3"))
                .prixUnitaire(new BigDecimal("11.93"))
                .idEntreprise(1L)
                .build();
        Vente venteMultiLignes = Vente.builder()
                .id(1L)
                .code("VT-2026-0001")
                .idEntreprise(1L)
                .lignes(new ArrayList<>(List.of(ligneVente, ligne2)))
                .build();

        VenteRequest multiLineRequest = new VenteRequest(
                null, "Vente comptoir",
                List.of(new LigneVenteRequest(1L, new BigDecimal("2")), new LigneVenteRequest(2L, new BigDecimal("3")))
        );
        ArticleResponse articleResponse2 = new ArticleResponse(2L, "ART-02", "Souris",
                new BigDecimal("10.00"), new BigDecimal("19.25"), new BigDecimal("11.93"), null, new BigDecimal("5"), 1L, "Informatique");

        when(venteRepository.countByCodeStartingWithAndIdEntreprise(anyString(), eq(ENTREPRISE_ID))).thenReturn(0L);
        when(venteMapper.toEntity(multiLineRequest)).thenReturn(new Vente());
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(articleService.getById(2L)).thenReturn(articleResponse2);
        when(entityManager.getReference(eq(Article.class), any())).thenReturn(article);
        when(venteRepository.save(any(Vente.class))).thenReturn(venteMultiLignes);
        when(venteMapper.toResponse(venteMultiLignes)).thenReturn(response);

        venteService.create(multiLineRequest);

        verify(mvtStkService, times(2)).sortieStock(any(MvtStkRequest.class));
    }

    @Test
    void create_shouldThrowEntityNotFoundException_andNotSaveVente_whenArticleNotFound() {
        when(venteRepository.countByCodeStartingWithAndIdEntreprise(anyString(), eq(ENTREPRISE_ID))).thenReturn(0L);
        when(venteMapper.toEntity(request)).thenReturn(new Vente());
        when(articleService.getById(1L)).thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 1"));

        assertThatThrownBy(() -> venteService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("1");

        verify(venteRepository, never()).save(any());
        verify(mvtStkService, never()).sortieStock(any());
    }

    // ------------------------------------------------------------------
    // chercherParId (getById) / chercherParCode (getByCode)
    // ------------------------------------------------------------------

    @Test
    void getById_shouldReturnVenteResponse_whenFound() {
        when(venteRepository.findByIdAndIdEntreprise(1L, ENTREPRISE_ID)).thenReturn(Optional.of(vente));
        when(venteMapper.toResponse(vente)).thenReturn(response);

        VenteResponse result = venteService.getById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(venteRepository.findByIdAndIdEntreprise(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venteService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getByCode_shouldReturnVenteResponse_whenFound() {
        when(venteRepository.findByCodeAndIdEntreprise("VT-2026-0001", ENTREPRISE_ID)).thenReturn(Optional.of(vente));
        when(venteMapper.toResponse(vente)).thenReturn(response);

        VenteResponse result = venteService.getByCode("VT-2026-0001");

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getByCode_shouldThrowEntityNotFoundException_whenNotFound() {
        when(venteRepository.findByCodeAndIdEntreprise("VT-INEXISTANT", ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venteService.getByCode("VT-INEXISTANT"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("VT-INEXISTANT");
    }

    // ------------------------------------------------------------------
    // supprimerVente (delete)
    // ------------------------------------------------------------------

    @Test
    void delete_shouldDeleteVente_whenFound() {
        when(venteRepository.findByIdAndIdEntreprise(1L, ENTREPRISE_ID)).thenReturn(Optional.of(vente));

        venteService.delete(1L);

        verify(venteRepository, times(1)).delete(vente);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotFound() {
        when(venteRepository.findByIdAndIdEntreprise(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venteService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(venteRepository, never()).delete(any());
    }
}
