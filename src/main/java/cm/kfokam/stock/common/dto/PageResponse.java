package cm.kfokam.stock.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Enveloppe de réponse paginée exposée par tous les endpoints de listing de l'API.
 * <p>
 * Remplace la sérialisation par défaut de {@link Page} (qui expose des détails internes de
 * Spring Data comme {@code pageable}, {@code sort} ou {@code empty}) par une structure plate,
 * stable et simple à typer côté frontend :
 * <pre>{@code
 * interface PageResponse<T> {
 *   content: T[];
 *   pageNumber: number;
 *   pageSize: number;
 *   totalElements: number;
 *   totalPages: number;
 *   isLast: boolean;
 * }
 * }</pre>
 */
public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLast
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
