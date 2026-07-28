package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.article.ArticleService;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.exception.StockInsuffisantException;
import cm.kfokam.stock.mvtstk.dto.AlerteStockResponse;
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
    private MvtStkResponse response;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);

        article = Article.builder().id(1L).code("ART-01").designation("Ordinateur portable").build();
        articleResponse = new ArticleResponse(1L, "ART-01", "Ordinateur portable",
                new BigDecimal("500.00"), new BigDecimal("19.25"), new BigDecimal("596.25"),
                null, new BigDecimal("5"), 1L, "Informatique");

        request = new MvtStkRequest(1L, new BigDecimal("5"), SourceMvtStk.COMMANDE_FOURNISSEUR);

        response = new MvtStkResponse(1L, Instant.now(), new BigDecimal("5"), 1L, "Ordinateur portable",
                TypeMvtStk.ENTREE, SourceMvtStk.COMMANDE_FOURNISSEUR, 1L);
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
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID)).thenReturn(mouvements);

        BigDecimal result = mvtStkService.stockReelArticle(1L);

        assertThat(result).isEqualByComparingTo(new BigDecimal("8"));
    }

    @Test
    void stockReelArticle_shouldReturnZero_whenNoMovements() {
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID)).thenReturn(List.of());

        BigDecimal result = mvtStkService.stockReelArticle(1L);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void stockReelArticle_shouldThrowEntityNotFoundException_whenArticleNotFound() {
        when(articleService.getById(99L)).thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 99"));

        assertThatThrownBy(() -> mvtStkService.stockReelArticle(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(mvtStkRepository, never()).findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(any(), any());
    }

    @Test
    void mvtStkArticle_shouldReturnListOfResponses() {
        List<MvtStk> mouvements = List.of(mouvement(TypeMvtStk.ENTREE, "10"));
        List<MvtStkResponse> responses = List.of(response);

        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID)).thenReturn(mouvements);
        when(mvtStkMapper.toResponseList(mouvements)).thenReturn(responses);

        List<MvtStkResponse> result = mvtStkService.mvtStkArticle(1L);

        assertThat(result).containsExactly(response);
    }

    @Test
    void mvtStkArticle_shouldThrowEntityNotFoundException_whenArticleNotFound() {
        when(articleService.getById(99L)).thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 99"));

        assertThatThrownBy(() -> mvtStkService.mvtStkArticle(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(mvtStkRepository, never()).findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(any(), any());
    }

    @Test
    void entreeStock_shouldCreateMovement_withTypeEntree() {
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkMapper.toEntity(request)).thenReturn(new MvtStk());
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(mvtStkRepository.save(any(MvtStk.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mvtStkMapper.toResponse(any(MvtStk.class))).thenReturn(response);

        MvtStkResponse result = mvtStkService.entreeStock(request);

        assertThat(result).isEqualTo(response);
        verify(mvtStkRepository).save(argThat(mvt -> mvt.getTypeMvt() == TypeMvtStk.ENTREE));
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
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID))
                .thenReturn(List.of(mouvement(TypeMvtStk.ENTREE, "10")));
        when(mvtStkMapper.toEntity(request)).thenReturn(new MvtStk());
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(mvtStkRepository.save(any(MvtStk.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mvtStkMapper.toResponse(any(MvtStk.class))).thenReturn(response);

        mvtStkService.sortieStock(request);

        verify(mvtStkRepository).save(argThat(mvt -> mvt.getTypeMvt() == TypeMvtStk.SORTIE));
    }

    @Test
    void sortieStock_shouldThrowStockInsuffisantException_whenStockTooLow() {
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID))
                .thenReturn(List.of(mouvement(TypeMvtStk.ENTREE, "2")));

        assertThatThrownBy(() -> mvtStkService.sortieStock(request))
                .isInstanceOf(StockInsuffisantException.class)
                .hasMessageContaining("Ordinateur portable");

        verify(mvtStkRepository, never()).save(any());
    }

    @Test
    void correctionStockPos_shouldCreateMovement_withTypeCorrectionPos() {
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkMapper.toEntity(request)).thenReturn(new MvtStk());
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(mvtStkRepository.save(any(MvtStk.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mvtStkMapper.toResponse(any(MvtStk.class))).thenReturn(response);

        mvtStkService.correctionStockPos(request);

        verify(mvtStkRepository).save(argThat(mvt -> mvt.getTypeMvt() == TypeMvtStk.CORRECTION_POS));
    }

    @Test
    void correctionStockNeg_shouldCreateMovement_withTypeCorrectionNeg() {
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID))
                .thenReturn(List.of(mouvement(TypeMvtStk.ENTREE, "10")));
        when(mvtStkMapper.toEntity(request)).thenReturn(new MvtStk());
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(mvtStkRepository.save(any(MvtStk.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mvtStkMapper.toResponse(any(MvtStk.class))).thenReturn(response);

        mvtStkService.correctionStockNeg(request);

        verify(mvtStkRepository).save(argThat(mvt -> mvt.getTypeMvt() == TypeMvtStk.CORRECTION_NEG));
    }

    @Test
    void correctionStockNeg_shouldThrowStockInsuffisantException_whenStockTooLow() {
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> mvtStkService.correctionStockNeg(request))
                .isInstanceOf(StockInsuffisantException.class);

        verify(mvtStkRepository, never()).save(any());
    }

    @Test
    void articlesEnAlerte_shouldReturnOnlyArticlesAtOrBelowSeuil() {
        ArticleResponse articleBas = new ArticleResponse(2L, "ART-02", "Souris",
                new BigDecimal("10.00"), new BigDecimal("19.25"), new BigDecimal("11.93"),
                null, new BigDecimal("5"), 1L, "Informatique");

        when(articleService.getAll()).thenReturn(List.of(articleResponse, articleBas));
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(1L, ENTREPRISE_ID))
                .thenReturn(List.of(mouvement(TypeMvtStk.ENTREE, "50")));
        when(mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(2L, ENTREPRISE_ID))
                .thenReturn(List.of());

        List<AlerteStockResponse> result = mvtStkService.articlesEnAlerte();

        assertThat(result).extracting(AlerteStockResponse::articleId).containsExactly(2L);
    }
}
