package pe.edu.unmsm.fisi.gestiondocente.docente.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import pe.edu.unmsm.fisi.gestiondocente.docente.exception.DocenteNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorResponse;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorResponseFactory;

@RestControllerAdvice(assignableTypes = { DocenteController.class, DirectorDocenteController.class })
public class DocenteExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException exception, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponseFactory.create(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DocenteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(DocenteNotFoundException exception, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponseFactory.create(
                exception.getStatus(),
                exception.getMessage(),
                request
        );
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }
}
