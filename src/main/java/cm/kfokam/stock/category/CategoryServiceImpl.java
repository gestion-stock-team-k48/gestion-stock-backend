package cm.kfokam.stock.category;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.category.dto.CategoryRequest;
import cm.kfokam.stock.category.dto.CategoryResponse;
import cm.kfokam.stock.category.model.Category;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CurrentUserService currentUserService;
    private final EntityManager entityManager;

    @Override
    public CategoryResponse create(CategoryRequest request) {
        Long idEntreprise = currentUserService.getCurrentEntrepriseId();
        if (categoryRepository.existsByCodeAndEntrepriseId(request.code(), idEntreprise)) {
            throw new DuplicateCodeException("Le code '%s' est déjà utilisé".formatted(request.code()));
        }
        Category category = categoryMapper.toEntity(request);
        category.setEntreprise(entityManager.getReference(Entreprise.class, idEntreprise));
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return categoryMapper.toResponse(findCategoryOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryMapper.toResponseList(
                categoryRepository.findAllByEntrepriseId(currentUserService.getCurrentEntrepriseId()));
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findCategoryOrThrow(id);

        categoryRepository.findByCodeAndEntrepriseId(request.code(), currentUserService.getCurrentEntrepriseId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateCodeException("Le code '%s' est déjà utilisé".formatted(request.code()));
                });

        categoryMapper.updateEntityFromRequest(request, category);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        Category category = findCategoryOrThrow(id);
        categoryRepository.delete(category);
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findByIdAndEntrepriseId(id, currentUserService.getCurrentEntrepriseId())
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable avec l'id : " + id));
    }
}
