package cm.kfokam.stock.article;

import cm.kfokam.stock.article.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ArticleRepository extends JpaRepository<Article, Long> {

    boolean existsByCode(String code);

    Optional<Article> findByCode(String code);
}