package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.ApprovedCertificateAlreadyExistsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.CertificateGenerationNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.CertificatePdfNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.GenerationAlreadyExistsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidStoragePathException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingRequiredFieldsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.PdfGenerationException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.StorageException;

@RestControllerAdvice(assignableTypes = { CourseCertificateController.class, ConstanciaQueryController.class })
public class ConstanciaExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidJson(HttpMessageNotReadableException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "JSON inválido"));
    }

    @ExceptionHandler(MissingRequiredFieldsException.class)
    public ResponseEntity<MissingFieldsErrorResponse> handleMissingFields(
            MissingRequiredFieldsException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MissingFieldsErrorResponse(exception.getMessage(), exception.getMissingFields()));
    }

    @ExceptionHandler(ApprovedCertificateAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleApprovedCertificate(
            ApprovedCertificateAlreadyExistsException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler({ CertificateGenerationNotFoundException.class, CertificatePdfNotFoundException.class })
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidStoragePathException.class)
    public ResponseEntity<Map<String, String>> handleInvalidStoragePath(InvalidStoragePathException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "La solicitud contiene identificadores inválidos"));
    }

    @ExceptionHandler(PdfGenerationException.class)
    public ResponseEntity<Map<String, String>> handlePdfGeneration(PdfGenerationException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "No se pudo generar el PDF de la constancia"));
    }

    @ExceptionHandler({ StorageException.class, GenerationAlreadyExistsException.class })
    public ResponseEntity<Map<String, String>> handleStorage(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "No se pudo almacenar la constancia"));
    }

    public static class MissingFieldsErrorResponse {

        private final String message;
        private final List<String> missingFields;

        public MissingFieldsErrorResponse(String message, List<String> missingFields) {
            this.message = message;
            this.missingFields = List.copyOf(missingFields);
        }

        public String getMessage() {
            return message;
        }

        public List<String> getMissingFields() {
            return missingFields;
        }
    }
}
