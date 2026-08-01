package cm.kfokam.stock.article;

import cm.kfokam.stock.article.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface ArticleRepository extends JpaRepository<Article, Long> {

    boolean existsByCodeAndEntrepriseId(String code, Long entrepriseId);

    Optional<Article> findByCodeAndEntrepriseId(String code, Long entrepriseId);

    Optional<Article> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    List<Article> findAllByEntrepriseId(Long entrepriseId);

    // LigneCommandeClient, LigneCommandeFournisseur, LigneVente et MvtStk sont des entités
    // publiques d'autres modules : les référencer en JPQL ici ne viole pas l'encapsulation des
    // repositories package-private (leurs Repository respectifs ne sont jamais impliqués).
    @Query("SELECT COUNT(l) > 0 FROM LigneCommandeClient l WHERE l.article.id = :articleId")
    boolean existsInCommandeClient(@Param("articleId") Long articleId);

    @Query("SELECT COUNT(l) > 0 FROM LigneCommandeFournisseur l WHERE l.article.id = :articleId")
    boolean existsInCommandeFournisseur(@Param("articleId") Long articleId);

    @Query("SELECT COUNT(l) > 0 FROM LigneVente l WHERE l.article.id = :articleId")
    boolean existsInVente(@Param("articleId") Long articleId);

    @Query("SELECT COUNT(m) > 0 FROM MvtStk m WHERE m.article.id = :articleId")
    boolean existsInMouvementStock(@Param("articleId") Long articleId);
}