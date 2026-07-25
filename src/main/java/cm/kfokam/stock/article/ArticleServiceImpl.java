package cm.kfokam.stock.article;

import cm.kfokam.stock.article.dto.ArticleRequest;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.category.CategoryRepository;
import cm.kfokam.stock.category.model.Category;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final ArticleMapper articleMapper;

    @Override
    public ArticleResponse create(ArticleRequest request) {
        if (articleRepository.existsByCode(request.code())) {
            throw new DuplicateCodeException("Le code '%s' est déjà utilisé".formatted(request.code()));
        }
        Category category = findCategoryOrThrow(request.categoryId());

        Article article = articleMapper.toEntity(request);
        article.setCategory(category);

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
        return articleMapper.toResponseList(articleRepository.findAll());
    }

    @Override
    public ArticleResponse update(Long id, ArticleRequest request) {
        Article article = findArticleOrThrow(id);

        articleRepository.findByCode(request.code())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateCodeException("Le code '%s' est déjà utilisé".formatted(request.code()));
                });

        Category category = findCategoryOrThrow(request.categoryId());

        articleMapper.updateEntityFromRequest(request, article);
        article.setCategory(category);

        return articleMapper.toResponse(articleRepository.save(article));
    }

    @Override
    public void delete(Long id) {
        Article article = findArticleOrThrow(id);
        articleRepository.delete(article);
    }

    private Article findArticleOrThrow(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable avec l'id : " + id));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable avec l'id : " + categoryId));
    }
}