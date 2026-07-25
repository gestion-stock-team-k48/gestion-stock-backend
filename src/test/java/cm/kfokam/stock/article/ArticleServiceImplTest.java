package cm.kfokam.stock.article;

import cm.kfokam.stock.article.dto.ArticleRequest;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.category.CategoryRepository;
import cm.kfokam.stock.category.model.Category;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ArticleMapper articleMapper;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private Category category;
    private Article article;
    private ArticleRequest request;
    private ArticleResponse response;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .code("CAT-01")
                .designation("Informatique")
                .build();

        article = Article.builder()
                .id(1L)
                .code("ART-01")
                .designation("Ordinateur portable")
                .prixUnitaireHt(new BigDecimal("500.00"))
                .tauxTva(new BigDecimal("19.25"))
                .prixUnitaireTtc(new BigDecimal("596.25"))
                .photo("photo.png")
                .category(category)
                .build();

        request = new ArticleRequest(
                "ART-01",
                "Ordinateur portable",
                new BigDecimal("500.00"),
                new BigDecimal("19.25"),
                new BigDecimal("596.25"),
                "photo.png",
                1L
        );

        response = new ArticleResponse(
                1L,
                "ART-01",
                "Ordinateur portable",
                new BigDecimal("500.00"),
                new BigDecimal("19.25"),
                new BigDecimal("596.25"),
                "photo.png",
                1L,
                "Informatique"
        );
    }

    @Test
    void create_shouldReturnResponse_whenValid() {
        when(articleRepository.existsByCode("ART-01")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(articleMapper.toEntity(request)).thenReturn(article);
        when(articleRepository.save(article)).thenReturn(article);
        when(articleMapper.toResponse(article)).thenReturn(response);

        ArticleResponse result = articleService.create(request);

        assertThat(result).isEqualTo(response);
        verify(articleRepository).save(article);
    }

    @Test
    void create_shouldThrowDuplicateCodeException_whenCodeAlreadyUsed() {
        when(articleRepository.existsByCode("ART-01")).thenReturn(true);

        assertThatThrownBy(() -> articleService.create(request))
                .isInstanceOf(DuplicateCodeException.class)
                .hasMessageContaining("ART-01");

        verify(categoryRepository, never()).findById(any());
        verify(articleRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowEntityNotFoundException_whenCategoryNotFound() {
        when(articleRepository.existsByCode("ART-01")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("1");

        verify(articleRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleMapper.toResponse(article)).thenReturn(response);

        ArticleResponse result = articleService.getById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnListOfResponses() {
        List<Article> articles = List.of(article);
        List<ArticleResponse> responses = List.of(response);

        when(articleRepository.findAll()).thenReturn(articles);
        when(articleMapper.toResponseList(articles)).thenReturn(responses);

        List<ArticleResponse> result = articleService.getAll();

        assertThat(result).containsExactly(response);
    }

    @Test
    void update_shouldReturnUpdatedResponse_whenValid() {
        ArticleRequest updateRequest = new ArticleRequest(
                "ART-02", "Ordinateur fixe", new BigDecimal("400.00"),
                new BigDecimal("19.25"), new BigDecimal("476.90"), "photo2.png", 1L
        );
        Article updatedArticle = Article.builder()
                .id(1L).code("ART-02").designation("Ordinateur fixe")
                .prixUnitaireHt(new BigDecimal("400.00")).tauxTva(new BigDecimal("19.25"))
                .prixUnitaireTtc(new BigDecimal("476.90")).photo("photo2.png").category(category)
                .build();
        ArticleResponse updatedResponse = new ArticleResponse(
                1L, "ART-02", "Ordinateur fixe", new BigDecimal("400.00"),
                new BigDecimal("19.25"), new BigDecimal("476.90"), "photo2.png", 1L, "Informatique"
        );

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.findByCode("ART-02")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doAnswer(invocation -> {
            article.setCode("ART-02");
            article.setDesignation("Ordinateur fixe");
            article.setPrixUnitaireHt(new BigDecimal("400.00"));
            article.setPrixUnitaireTtc(new BigDecimal("476.90"));
            article.setPhoto("photo2.png");
            return null;
        }).when(articleMapper).updateEntityFromRequest(updateRequest, article);
        when(articleRepository.save(article)).thenReturn(updatedArticle);
        when(articleMapper.toResponse(updatedArticle)).thenReturn(updatedResponse);

        ArticleResponse result = articleService.update(1L, updateRequest);

        assertThat(result).isEqualTo(updatedResponse);
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenArticleNotFound() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(articleRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowDuplicateCodeException_whenCodeUsedByAnotherArticle() {
        Article otherArticle = Article.builder().id(2L).code("ART-02").designation("Autre").build();
        ArticleRequest updateRequest = new ArticleRequest(
                "ART-02", "Ordinateur portable", new BigDecimal("500.00"),
                new BigDecimal("19.25"), new BigDecimal("596.25"), "photo.png", 1L
        );

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.findByCode("ART-02")).thenReturn(Optional.of(otherArticle));

        assertThatThrownBy(() -> articleService.update(1L, updateRequest))
                .isInstanceOf(DuplicateCodeException.class)
                .hasMessageContaining("ART-02");

        verify(articleRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenCategoryNotFound() {
        ArticleRequest updateRequest = new ArticleRequest(
                "ART-01", "Ordinateur portable", new BigDecimal("500.00"),
                new BigDecimal("19.25"), new BigDecimal("596.25"), "photo.png", 99L
        );

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.findByCode("ART-01")).thenReturn(Optional.of(article));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.update(1L, updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(articleRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameCode_whenCodeUnchanged() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.findByCode("ART-01")).thenReturn(Optional.of(article));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(articleRepository.save(article)).thenReturn(article);
        when(articleMapper.toResponse(article)).thenReturn(response);

        ArticleResponse result = articleService.update(1L, request);

        assertThat(result).isEqualTo(response);
        verify(articleMapper).updateEntityFromRequest(request, article);
    }

    @Test
    void delete_shouldDeleteArticle_whenFound() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        articleService.delete(1L);

        verify(articleRepository, times(1)).delete(article);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotFound() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(articleRepository, never()).delete(any());
    }
}