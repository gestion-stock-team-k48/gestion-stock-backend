package cm.kfokam.stock.category;

import cm.kfokam.stock.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryRepository extends JpaRepository<Category, Long> {
}
