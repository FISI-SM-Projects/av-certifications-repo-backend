package pe.edu.unmsm.fisi.gestiondocente.shared.response;

import org.springframework.data.domain.Page;

public record Pagination(
        Integer pageNumber,
        Integer pageSize,
        Long totalElements,
        Integer totalPages,
        Integer numberOfElements
) {
    public static Pagination fromPage(Page<?> page) {
        return new Pagination(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumberOfElements()
        );
    }
}
