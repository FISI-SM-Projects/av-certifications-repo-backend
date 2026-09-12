package pe.edu.unmsm.fisi.gestiondocente.shared.response;

public record PaginatedResponse<T>(
        Boolean success,
        String message,
        T data,
        Pagination pagination
) {
    public static <T> PaginatedResponse<T> success(String message, T data, Pagination pagination) {
        return new PaginatedResponse<>(true, message, data, pagination);
    }

    public record Pagination(
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            int numberOfElements
    ) {
    }
}
