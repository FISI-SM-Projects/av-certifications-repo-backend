package pe.edu.unmsm.fisi.gestiondocente.shared.response;

import java.time.OffsetDateTime;

public record ErrorResponse(
        Boolean success,
        Integer statusCode,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp,
        ErrorDetails[] details
) {
}
