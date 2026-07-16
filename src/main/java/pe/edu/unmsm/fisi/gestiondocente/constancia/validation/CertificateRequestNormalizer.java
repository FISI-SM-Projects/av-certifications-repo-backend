package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import java.util.Locale;

public final class CertificateRequestNormalizer {

    private CertificateRequestNormalizer() {
    }

    public static String text(String value) {
        return trimToEmpty(value);
    }

    public static String code(String value) {
        return trimToEmpty(value).toUpperCase(Locale.ROOT);
    }

    public static String email(String value) {
        return trimToEmpty(value).toLowerCase(Locale.ROOT);
    }

    public static String normalizedNameForComparison(String value) {
        return trimToEmpty(value).replaceAll("\\s+", " ");
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
