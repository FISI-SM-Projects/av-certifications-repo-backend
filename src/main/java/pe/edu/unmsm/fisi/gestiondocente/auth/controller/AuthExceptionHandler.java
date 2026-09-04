package pe.edu.unmsm.fisi.gestiondocente.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import pe.edu.unmsm.fisi.gestiondocente.auth.exception.InvalidCredentialsException;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorDetails;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorResponse;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorResponseFactory;

import java.util.List;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        ErrorDetails[] details = fieldErrors.stream()
                .map(error -> new ErrorDetails(error.getField(), error.getDefaultMessage()))
                .toArray(ErrorDetails[]::new);

        ErrorResponse errorResponse = ErrorResponseFactory.create(
                HttpStatus.BAD_REQUEST,
                "Errores de validación en la solicitud",
                request,
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponseFactory.create(
                exception.getStatus(),
                exception.getMessage() != null ? exception.getMessage() : "Credenciales inválidas",
                request
        );

        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception, WebRequest request) {
        log.error("Error inesperado en AuthController: ", exception);

        ErrorResponse errorResponse = ErrorResponseFactory.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado al procesar la solicitud",
                request
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
