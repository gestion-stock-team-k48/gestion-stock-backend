package cm.kfokam.stock.category;

import cm.kfokam.stock.category.dto.CategoryRequest;
import cm.kfokam.stock.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse getById(Long id);

    List<CategoryResponse> getAll();

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
