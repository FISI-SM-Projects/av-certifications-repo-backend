package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidStoragePathException;

@Component
public class StoragePathSanitizer {

    private static final Pattern SAFE_SEGMENT_PATTERN = Pattern.compile("[A-Za-z0-9._-]+");
    private static final Set<String> WINDOWS_RESERVED_NAMES = Set.of(
            "CON", "PRN", "AUX", "NUL", "CLOCK$",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9");

    public String sanitizeSegment(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidStoragePathException("El segmento de ruta es obligatorio");
        }

        if (value.endsWith(".") || value.endsWith(" ")) {
            throw new InvalidStoragePathException("El segmento de ruta contiene caracteres no permitidos");
        }

        String sanitized = value.trim();

        if (sanitized.length() > CertificateRequestValidationRules.STORAGE_SEGMENT_MAX
                || sanitized.contains("..")
                || sanitized.endsWith(".")
                || sanitized.endsWith(" ")
                || sanitized.startsWith("/")
                || sanitized.startsWith("\\")
                || sanitized.contains("/")
                || sanitized.contains("\\")
                || containsControlCharacter(sanitized)
                || !SAFE_SEGMENT_PATTERN.matcher(sanitized).matches()
                || isWindowsReservedName(sanitized)) {
            throw new InvalidStoragePathException("El segmento de ruta contiene caracteres no permitidos");
        }

        return sanitized;
    }

    private boolean isWindowsReservedName(String value) {
        String upperValue = value.toUpperCase(Locale.ROOT);
        int dotIndex = upperValue.indexOf('.');
        String baseName = dotIndex >= 0 ? upperValue.substring(0, dotIndex) : upperValue;
        return WINDOWS_RESERVED_NAMES.contains(baseName);
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
