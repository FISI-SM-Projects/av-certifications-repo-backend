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
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.CertificateSourceInconsistencyException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.DuplicateExpectedCoursesException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.GenerationAlreadyExistsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidPdfContentException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidStoragePathException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidRequestFieldsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingExpectedCoursesException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingRequiredFieldsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.PdfGenerationException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.StorageException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.TeacherIdentityMismatchException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.TeacherNotFoundForCertificateException;

@RestControllerAdvice(assignableTypes = {
        CourseCertificateController.class,
        SemesterCertificateController.class,
        ConstanciaQueryController.class
})
public class ConstanciaExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidJson(HttpMessageNotReadableException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "JSON inválido"));
    }

    @ExceptionHandler(MissingExpectedCoursesException.class)
    public ResponseEntity<MissingCoursesErrorResponse> handleMissingCourses(
            MissingExpectedCoursesException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new MissingCoursesErrorResponse(exception.getMessage(), exception.getMissingCourses()));
    }

    @ExceptionHandler(DuplicateExpectedCoursesException.class)
    public ResponseEntity<DuplicateCoursesErrorResponse> handleDuplicateCourses(
            DuplicateExpectedCoursesException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new DuplicateCoursesErrorResponse(exception.getMessage(), exception.getDuplicateCourses()));
    }

    @ExceptionHandler(CertificateSourceInconsistencyException.class)
    public ResponseEntity<Map<String, String>> handleSourceInconsistency(
            CertificateSourceInconsistencyException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(MissingRequiredFieldsException.class)
    public ResponseEntity<MissingFieldsErrorResponse> handleMissingFields(
            MissingRequiredFieldsException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MissingFieldsErrorResponse(exception.getMessage(), exception.getMissingFields()));
    }

    @ExceptionHandler(InvalidRequestFieldsException.class)
    public ResponseEntity<InvalidFieldsErrorResponse> handleInvalidFields(
            InvalidRequestFieldsException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new InvalidFieldsErrorResponse(exception.getMessage(), exception.getInvalidFields()));
    }

    @ExceptionHandler(TeacherIdentityMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTeacherIdentityMismatch(
            TeacherIdentityMismatchException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(TeacherNotFoundForCertificateException.class)
    public ResponseEntity<Map<String, String>> handleTeacherNotFound(
            TeacherNotFoundForCertificateException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
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

    @ExceptionHandler(GenerationAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleGenerationAlreadyExists(
            GenerationAlreadyExistsException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", "La generacion de constancia ya existe"));
    }

    @ExceptionHandler(InvalidPdfContentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPdf(InvalidPdfContentException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "No se pudo generar un PDF valido de la constancia"));
    }

    @ExceptionHandler(StorageException.class)
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

    public static class MissingCoursesErrorResponse {

        private final String message;
        private final List<?> missingCourses;

        public MissingCoursesErrorResponse(String message, List<?> missingCourses) {
            this.message = message;
            this.missingCourses = List.copyOf(missingCourses);
        }

        public String getMessage() {
            return message;
        }

        public List<?> getMissingCourses() {
            return missingCourses;
        }
    }

    public static class DuplicateCoursesErrorResponse {

        private final String message;
        private final List<?> duplicateCourses;

        public DuplicateCoursesErrorResponse(String message, List<?> duplicateCourses) {
            this.message = message;
            this.duplicateCourses = List.copyOf(duplicateCourses);
        }

        public String getMessage() {
            return message;
        }

        public List<?> getDuplicateCourses() {
            return duplicateCourses;
        }
    }

    public static class InvalidFieldsErrorResponse {

        private final String message;
        private final List<?> invalidFields;

        public InvalidFieldsErrorResponse(String message, List<?> invalidFields) {
            this.message = message;
            this.invalidFields = List.copyOf(invalidFields);
        }

        public String getMessage() {
            return message;
        }

        public List<?> getInvalidFields() {
            return invalidFields;
        }
    }
}
