package pe.edu.unmsm.fisi.gestiondocente.shared.response;

public record ErrorDetails(
        String field,
        String message
) {
}
