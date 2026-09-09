package pe.edu.unmsm.fisi.gestiondocente.shared.controller;

import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = {
    pe.edu.unmsm.fisi.gestiondocente.auth.controller.CurrentUserController.class,
    pe.edu.unmsm.fisi.gestiondocente.docente.controller.InstitutionalTeacherController.class,
    pe.edu.unmsm.fisi.gestiondocente.constancia.controller.InstitutionalCertificateController.class
})
public class InstitutionalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> known(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", ex.getReason() == null ? "Solicitud no disponible" : ex.getReason()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> unexpected(Exception ex) {
        LoggerFactory.getLogger(getClass()).error("Error en operacion institucional", ex);
        return ResponseEntity.status(500).body(Map.of("message", "No se pudo completar la operacion institucional"));
    }
}
