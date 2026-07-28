package cm.kfokam.stock.category;

import cm.kfokam.stock.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCodeAndEntrepriseId(String code, Long entrepriseId);

    Optional<Category> findByCodeAndEntrepriseId(String code, Long entrepriseId);

    Optional<Category> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    List<Category> findAllByEntrepriseId(Long entrepriseId);
}
