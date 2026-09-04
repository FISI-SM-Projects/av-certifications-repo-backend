package pe.edu.unmsm.fisi.gestiondocente.shared.response;

public record DefaultResponse<T>(
        Boolean success,
        String message,
        T data
) {
    public static <T> DefaultResponse<T> success(String message, T data) {
        return new DefaultResponse<>(true, message, data);
    }
}