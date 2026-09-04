package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorResponse;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorResponseFactory;

@RestControllerAdvice(assignableTypes = {
        CourseCertificateController.class,
        SemesterCertificateController.class,
        ConstanciaQueryController.class
})
public class ConstanciaExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException exception, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponseFactory.create(
                HttpStatus.BAD_REQUEST,
                "JSON inválido",
                request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BaseDomainException.class)
    public ResponseEntity<ErrorResponse> handleConstanciaDomainExceptions(BaseDomainException exception, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponseFactory.create(
                exception.getStatus(),
                exception.getMessage(),
                request,
                exception.getDetails()
        );
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }
}
