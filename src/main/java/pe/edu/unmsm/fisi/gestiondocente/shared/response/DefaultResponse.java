package pe.edu.unmsm.fisi.gestiondocente.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DefaultResponse<T>(
        Boolean success,
        String message,
        T data,
        Pagination pagination
) {
    public static <T> DefaultResponse<T> success(String message, T data) {
        return new DefaultResponse<>(true, message, data, null);
    }

    public static <T> DefaultResponse<List<T>> success(String message, Page<T> page) {
        return new DefaultResponse<>(
                true,
                message,
                page.getContent(),
                Pagination.fromPage(page)
        );
    }
}