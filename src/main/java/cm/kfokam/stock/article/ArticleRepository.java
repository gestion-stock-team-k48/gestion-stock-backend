package cm.kfokam.stock.article;

import cm.kfokam.stock.article.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface ArticleRepository extends JpaRepository<Article, Long> {

    boolean existsByCodeAndEntrepriseId(String code, Long entrepriseId);

    Optional<Article> findByCodeAndEntrepriseId(String code, Long entrepriseId);

    Optional<Article> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    List<Article> findAllByEntrepriseId(Long entrepriseId);
}