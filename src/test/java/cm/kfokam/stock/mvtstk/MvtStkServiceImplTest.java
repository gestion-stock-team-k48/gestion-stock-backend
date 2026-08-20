package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.article.ArticleService;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.exception.StockInsuffisantException;
import cm.kfokam.stock.mvtstk.dto.AlerteStockResponse;
import cm.kfokam.stock.mvtstk.dto.MvtStkCorrectionRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkResponse;
import cm.kfokam.stock.mvtstk.model.MvtStk;
import cm.kfokam.stock.mvtstk.model.SourceMvtStk;
import cm.kfokam.stock.mvtstk.model.TypeMvtStk;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MvtStkServiceImplTest {

    private static final Long ENTREPRISE_ID = 1L;

    @Mock
    private MvtStkRepository mvtStkRepository;

    @Mock
    private MvtStkMapper mvtStkMapper;

    @Mock
    private ArticleService articleService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private MvtStkServiceImpl mvtStkService;

    private Article article;
    private ArticleResponse articleResponse;
    private MvtStkRequest request;
    private MvtStkCorrectionRequest correctionRequest;
    private MvtStkResponse response;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);

        article = Article.builder().id(1L).code("ART-01").designation("Ordinateur portable").build();
        articleResponse = new ArticleResponse(1L, "ART-01", "Ordinateur portable",
                new BigDecimal("500.00"), new BigDecimal("19.25"), new BigDecimal("596.25"),
                null, new BigDecimal("5"), 1L, "Informatique", null, null, null, null);

        request = new MvtStkRequest(1L, new BigDecimal("5"), SourceMvtStk.COMMANDE_FOURNISSEUR);
        correctionRequest = new MvtStkCorrectionRequest(1L, new BigDecimal("5"), "Casse en entrepôt");

        response = new MvtStkResponse(1L, Instant.now(), new BigDecimal("5"), 1L, "Ordinateur portable",
                TypeMvtStk.ENTREE, SourceMvtStk.COMMANDE_FOURNISSEUR, null, 1L, null, null, null, null);

        lenient().when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        lenient().when(mvtStkRepository.save(any(MvtStk.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(mvtStkMapper.toResponse(any(MvtStk.class))).thenReturn(response);
    }

    private MvtStk mouvement(TypeMvtStk type, String quantite) {
        return MvtStk.builder()
                .article(article)
                .typeMvt(type)
                .quantite(new BigDecimal(quantite))
                .sourceMvt(SourceMvtStk.STOCK_INITIAL)
                .idEntreprise(1L)
                .dateMvt(Instant.now())
                .build();
    }

    @Test
    void stockReelArticle_shouldComputeAlgebraicSum_ofAllMovementTypes() {
        List<MvtStk> mouvements = List.of(
                mouvement(TypeMvtStk.ENTREE, "10"),
                mouvement(TypeMvtStk.SORTIE, "3"),
                mouvement(TypeMvtStk.CORRECTION_POS, "2"),
                mouvement(TypeMvtStk.CORRECTION_NEG, "1")
        );

        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(mouvements));

        BigDecimal result = mvtStkService.stockReelArticle(1L);

        assertThat(result).isEqualByComparingTo(new BigDecimal("8"));
    }

    @Test
    void stockReelArticle_shouldReturnZero_whenNoMovements() {
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));

        BigDecimal result = mvtStkService.stockReelArticle(1L);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void stockReelArticle_shouldThrowEntityNotFoundException_whenArticleNotFound() {
        when(articleService.getById(99L)).thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 99"));

        assertThatThrownBy(() -> mvtStkService.stockReelArticle(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(mvtStkRepository, never()).findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(any(), any(), any());
    }

    @Test
    void mvtStkArticle_shouldReturnPageOfResponses() {
        Pageable pageable = PageRequest.of(0, 20);
        List<MvtStk> mouvements = List.of(mouvement(TypeMvtStk.ENTREE, "10"));

        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID, pageable))
                .thenReturn(new PageImpl<>(mouvements));

        Page<MvtStkResponse> result = mvtStkService.mvtStkArticle(1L, pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void mvtStkArticle_shouldThrowEntityNotFoundException_whenArticleNotFound() {
        when(articleService.getById(99L)).thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 99"));

        assertThatThrownBy(() -> mvtStkService.mvtStkArticle(99L, Pageable.unpaged()))
                .isInstanceOf(EntityNotFoundException.class);

        verify(mvtStkRepository, never()).findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(any(), any(), any());
    }

    @Test
    void entreeStock_shouldCreateMovement_withTypeEntree() {
        when(articleService.getById(1L)).thenReturn(articleResponse);

        MvtStkResponse result = mvtStkService.entreeStock(request);

        assertThat(result).isEqualTo(response);
        verify(mvtStkRepository).save(argThat(mvt ->
                mvt.getTypeMvt() == TypeMvtStk.ENTREE
                        && mvt.getSourceMvt() == SourceMvtStk.COMMANDE_FOURNISSEUR
                        && mvt.getMotif() == null
        ));
    }

    @Test
    void entreeStock_shouldThrowEntityNotFoundException_whenArticleNotFound() {
        when(articleService.getById(1L)).thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 1"));

        assertThatThrownBy(() -> mvtStkService.entreeStock(request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(mvtStkRepository, never()).save(any());
    }

    @Test
    void sortieStock_shouldCreateMovement_withTypeSortie() {
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(mouvement(TypeMvtStk.ENTREE, "10"))));

        mvtStkService.sortieStock(request);

        verify(mvtStkRepository).save(argThat(mvt -> mvt.getTypeMvt() == TypeMvtStk.SORTIE));
    }

    @Test
    void sortieStock_shouldThrowStockInsuffisantException_whenStockTooLow() {
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(mouvement(TypeMvtStk.ENTREE, "2"))));

        assertThatThrownBy(() -> mvtStkService.sortieStock(request))
                .isInstanceOf(StockInsuffisantException.class)
                .hasMessageContaining("Ordinateur portable");

        verify(mvtStkRepository, never()).save(any());
    }

    @Test
    void correctionStockPos_shouldCreateMovement_withTypeCorrectionPosAndMotif() {
        when(articleService.getById(1L)).thenReturn(articleResponse);

        mvtStkService.correctionStockPos(correctionRequest);

        verify(mvtStkRepository).save(argThat(mvt ->
                mvt.getTypeMvt() == TypeMvtStk.CORRECTION_POS
                        && mvt.getSourceMvt() == SourceMvtStk.CORRECTION_MANUELLE
                        && "Casse en entrepôt".equals(mvt.getMotif())
        ));
    }

    @Test
    void correctionStockNeg_shouldCreateMovement_withTypeCorrectionNegAndMotif() {
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(mouvement(TypeMvtStk.ENTREE, "10"))));

        mvtStkService.correctionStockNeg(correctionRequest);

        verify(mvtStkRepository).save(argThat(mvt ->
                mvt.getTypeMvt() == TypeMvtStk.CORRECTION_NEG
                        && mvt.getSourceMvt() == SourceMvtStk.CORRECTION_MANUELLE
                        && "Casse en entrepôt".equals(mvt.getMotif())
        ));
    }

    @Test
    void correctionStockNeg_shouldThrowStockInsuffisantException_whenStockTooLow() {
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));

        assertThatThrownBy(() -> mvtStkService.correctionStockNeg(correctionRequest))
                .isInstanceOf(StockInsuffisantException.class);

        verify(mvtStkRepository, never()).save(any());
    }

    @Test
    void articlesEnAlerte_shouldReturnOnlyArticlesAtOrBelowSeuil() {
        ArticleResponse articleBas = new ArticleResponse(2L, "ART-02", "Souris",
                new BigDecimal("10.00"), new BigDecimal("19.25"), new BigDecimal("11.93"),
                null, new BigDecimal("5"), 1L, "Informatique", null, null, null, null);

        when(articleService.getAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(articleResponse, articleBas)));
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(mouvement(TypeMvtStk.ENTREE, "50"))));
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(2L, ENTREPRISE_ID, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));

        List<AlerteStockResponse> result = mvtStkService.articlesEnAlerte();

        assertThat(result).extracting(AlerteStockResponse::articleId).containsExactly(2L);
    }
}
