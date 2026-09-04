package pe.edu.unmsm.fisi.gestiondocente.shared.response;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;

public final class ErrorResponseFactory {

    private ErrorResponseFactory() {
        // Utility class
    }

    public static ErrorResponse create(HttpStatus status, String message, String path, ErrorDetails[] details) {
        return new ErrorResponse(
                false,
                status.value(),
                status.getReasonPhrase(),
                message,
                path != null ? path : "",
                OffsetDateTime.now(),
                details != null ? details : new ErrorDetails[0]
        );
    }

    public static ErrorResponse create(HttpStatus status, String message, String path) {
        return create(status, message, path, new ErrorDetails[0]);
    }

    public static ErrorResponse create(HttpStatus status, String message, WebRequest request, ErrorDetails[] details) {
        String path = extractPath(request);
        return create(status, message, path, details);
    }

    public static ErrorResponse create(HttpStatus status, String message, WebRequest request) {
        return create(status, message, request, new ErrorDetails[0]);
    }

    public static ErrorResponse create(HttpStatus status, String message, HttpServletRequest request, ErrorDetails[] details) {
        String path = request != null ? request.getRequestURI() : "";
        return create(status, message, path, details);
    }

    public static ErrorResponse create(HttpStatus status, String message, HttpServletRequest request) {
        return create(status, message, request, new ErrorDetails[0]);
    }

    private static String extractPath(WebRequest request) {
        if (request == null) {
            return "";
        }
        String description = request.getDescription(false);
        if (description != null && description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description != null ? description : "";
    }
}
