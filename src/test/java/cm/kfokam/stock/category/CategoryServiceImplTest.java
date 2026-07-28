package cm.kfokam.stock.category;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.category.dto.CategoryRequest;
import cm.kfokam.stock.category.dto.CategoryResponse;
import cm.kfokam.stock.category.model.Category;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    private static final Long ENTREPRISE_ID = 1L;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequest request;
    private CategoryResponse response;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);

        category = Category.builder()
                .id(1L)
                .code("CAT-01")
                .designation("Informatique")
                .build();

        request = new CategoryRequest("CAT-01", "Informatique");
        response = new CategoryResponse(1L, "CAT-01", "Informatique");
    }

    @Test
    void create_shouldReturnResponse_whenCodeNotUsed() {
        when(categoryRepository.existsByCodeAndEntrepriseId("CAT-01", ENTREPRISE_ID)).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.create(request);

        assertThat(result).isEqualTo(response);
        verify(categoryRepository).save(category);
    }

    @Test
    void create_shouldThrowDuplicateCodeException_whenCodeAlreadyUsed() {
        when(categoryRepository.existsByCodeAndEntrepriseId("CAT-01", ENTREPRISE_ID)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(DuplicateCodeException.class)
                .hasMessageContaining("CAT-01");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        when(categoryRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.getById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(categoryRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnListOfResponses() {
        List<Category> categories = List.of(category);
        List<CategoryResponse> responses = List.of(response);

        when(categoryRepository.findAllByEntrepriseId(ENTREPRISE_ID)).thenReturn(categories);
        when(categoryMapper.toResponseList(categories)).thenReturn(responses);

        List<CategoryResponse> result = categoryService.getAll();

        assertThat(result).containsExactly(response);
    }

    @Test
    void update_shouldReturnUpdatedResponse_whenValid() {
        CategoryRequest updateRequest = new CategoryRequest("CAT-02", "Bureautique");
        Category updatedCategory = Category.builder().id(1L).code("CAT-02").designation("Bureautique").build();
        CategoryResponse updatedResponse = new CategoryResponse(1L, "CAT-02", "Bureautique");

        when(categoryRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.findByCodeAndEntrepriseId("CAT-02", ENTREPRISE_ID)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            category.setCode("CAT-02");
            category.setDesignation("Bureautique");
            return null;
        }).when(categoryMapper).updateEntityFromRequest(updateRequest, category);
        when(categoryRepository.save(category)).thenReturn(updatedCategory);
        when(categoryMapper.toResponse(updatedCategory)).thenReturn(updatedResponse);

        CategoryResponse result = categoryService.update(1L, updateRequest);

        assertThat(result).isEqualTo(updatedResponse);
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenCategoryNotFound() {
        when(categoryRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowDuplicateCodeException_whenNewCodeUsedByAnotherCategory() {
        Category otherCategory = Category.builder().id(2L).code("CAT-02").designation("Autre").build();
        CategoryRequest updateRequest = new CategoryRequest("CAT-02", "Bureautique");

        when(categoryRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.findByCodeAndEntrepriseId("CAT-02", ENTREPRISE_ID)).thenReturn(Optional.of(otherCategory));

        assertThatThrownBy(() -> categoryService.update(1L, updateRequest))
                .isInstanceOf(DuplicateCodeException.class)
                .hasMessageContaining("CAT-02");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameCode_whenCodeUnchanged() {
        when(categoryRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.findByCodeAndEntrepriseId("CAT-01", ENTREPRISE_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.update(1L, request);

        assertThat(result).isEqualTo(response);
        verify(categoryMapper).updateEntityFromRequest(request, category);
    }

    @Test
    void delete_shouldDeleteCategory_whenFound() {
        when(categoryRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotFound() {
        when(categoryRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(categoryRepository, never()).delete(any());
    }
}
