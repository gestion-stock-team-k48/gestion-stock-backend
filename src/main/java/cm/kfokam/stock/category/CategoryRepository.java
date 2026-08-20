package cm.kfokam.stock.category;

import cm.kfokam.stock.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCodeAndEntrepriseId(String code, Long entrepriseId);

    Optional<Category> findByCodeAndEntrepriseId(String code, Long entrepriseId);

    Optional<Category> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    List<Category> findAllByEntrepriseId(Long entrepriseId);

    // Article est une entité publique d'un autre module : la référencer en JPQL ici ne viole pas
    // l'encapsulation des repositories package-private (ArticleRepository n'est jamais impliqué).
    @Query("SELECT COUNT(a) > 0 FROM Article a WHERE a.category.id = :categoryId")
    boolean existsArticleForCategory(@Param("categoryId") Long categoryId);
}
