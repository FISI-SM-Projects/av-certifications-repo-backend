package pe.edu.unmsm.fisi.gestiondocente.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorDetails;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorResponse;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorResponseFactory;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BaseDomainException.class)
    public ResponseEntity<ErrorResponse> handleBaseDomainException(BaseDomainException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponseFactory.create(
                ex.getStatus(),
                ex.getMessage(),
                request,
                ex.getDetails()
        );
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        ErrorDetails[] details = fieldErrors.stream()
                .map(error -> new ErrorDetails(error.getField(), error.getDefaultMessage()))
                .toArray(ErrorDetails[]::new);

        ErrorResponse errorResponse = ErrorResponseFactory.create(
                HttpStatus.BAD_REQUEST,
                "Error de validación en los campos enviados.",
                request,
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalErrors(Exception ex, WebRequest request) {
        log.error("Unhandled exception caught in GlobalExceptionHandler: ", ex);
        ErrorResponse errorResponse = ErrorResponseFactory.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() != null ? ex.getMessage() : "Ocurrió un error inesperado al procesar la solicitud",
                request
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
