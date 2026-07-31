package cm.kfokam.stock.article;

import cm.kfokam.stock.article.dto.ArticleRequest;
import cm.kfokam.stock.article.dto.ArticleResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArticleService {

    ArticleResponse create(ArticleRequest request);

    ArticleResponse getById(Long id);

    List<ArticleResponse> getAll();

    ArticleResponse update(Long id, ArticleRequest request);

    ArticleResponse uploadPhoto(Long id, MultipartFile file);

    void delete(Long id);
}