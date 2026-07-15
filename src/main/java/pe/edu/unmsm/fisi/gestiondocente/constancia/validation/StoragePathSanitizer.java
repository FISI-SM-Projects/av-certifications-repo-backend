package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidStoragePathException;

@Component
public class StoragePathSanitizer {

    public String sanitizeSegment(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidStoragePathException("El segmento de ruta es obligatorio");
        }

        String sanitized = value.trim();

        if (sanitized.contains("..") || sanitized.contains("/") || sanitized.contains("\\")
                || containsControlCharacter(sanitized)) {
            throw new InvalidStoragePathException("El segmento de ruta contiene caracteres no permitidos");
        }

        return sanitized;
    }

    private boolean containsControlCharacter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                return true;
            }
        }

        return false;
    }
}
