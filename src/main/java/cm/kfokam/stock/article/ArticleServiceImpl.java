package cm.kfokam.stock.article;

import cm.kfokam.stock.article.dto.ArticleRequest;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.category.CategoryService;
import cm.kfokam.stock.category.dto.CategoryResponse;
import cm.kfokam.stock.category.model.Category;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryService categoryService;
    private final ArticleMapper articleMapper;
    private final CurrentUserService currentUserService;
    private final EntityManager entityManager;

    @Override
    public ArticleResponse create(ArticleRequest request) {
        Long idEntreprise = currentUserService.getCurrentEntrepriseId();
        if (articleRepository.existsByCodeAndEntrepriseId(request.code(), idEntreprise)) {
            throw new DuplicateCodeException("Le code '%s' est déjà utilisé".formatted(request.code()));
        }
        CategoryResponse category = findCategoryOrThrow(request.categoryId());

        Article article = articleMapper.toEntity(request);
        article.setCategory(entityManager.getReference(Category.class, category.id()));
        article.setEntreprise(entityManager.getReference(Entreprise.class, idEntreprise));

        Article saved = articleRepository.save(article);
        return articleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleResponse getById(Long id) {
        return articleMapper.toResponse(findArticleOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponse> getAll() {
        return articleMapper.toResponseList(
                articleRepository.findAllByEntrepriseId(currentUserService.getCurrentEntrepriseId()));
    }

    @Override
    public ArticleResponse update(Long id, ArticleRequest request) {
        Article article = findArticleOrThrow(id);

        articleRepository.findByCodeAndEntrepriseId(request.code(), currentUserService.getCurrentEntrepriseId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateCodeException("Le code '%s' est déjà utilisé".formatted(request.code()));
                });

        CategoryResponse category = findCategoryOrThrow(request.categoryId());

        articleMapper.updateEntityFromRequest(request, article);
        article.setCategory(entityManager.getReference(Category.class, category.id()));

        return articleMapper.toResponse(articleRepository.save(article));
    }

    @Override
    public void delete(Long id) {
        Article article = findArticleOrThrow(id);
        articleRepository.delete(article);
    }

    private Article findArticleOrThrow(Long id) {
        return articleRepository.findByIdAndEntrepriseId(id, currentUserService.getCurrentEntrepriseId())
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable avec l'id : " + id));
    }

    private CategoryResponse findCategoryOrThrow(Long categoryId) {
        return categoryService.getById(categoryId);
    }
}