package cm.kfokam.stock.article;

import cm.kfokam.stock.article.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;

interface ArticleRepository extends JpaRepository<Article, Long> {
}
