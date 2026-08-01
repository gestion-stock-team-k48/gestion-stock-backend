package cm.kfokam.stock.article;

import cm.kfokam.stock.article.dto.ArticleRequest;
import cm.kfokam.stock.article.dto.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ArticleService {

    ArticleResponse create(ArticleRequest request);

    ArticleResponse getById(Long id);

    Page<ArticleResponse> getAll(Pageable pageable);

    ArticleResponse update(Long id, ArticleRequest request);

    ArticleResponse uploadPhoto(Long id, MultipartFile file);

    void delete(Long id);
}