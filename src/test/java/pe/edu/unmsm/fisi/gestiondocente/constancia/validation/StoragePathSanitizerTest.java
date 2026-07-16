package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidStoragePathException;

class StoragePathSanitizerTest {

    private final StoragePathSanitizer storagePathSanitizer = new StoragePathSanitizer();

    @ParameterizedTest
    @ValueSource(strings = { "22200275", "32BGNYGF", "1", "26.1", "SW" })
    void debeAceptarSegmentosNormales(String value) {
        assertThat(storagePathSanitizer.sanitizeSegment(value)).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../", "..", "abc/def", "abc\\def", "", "   ", "abc\u0000def",
            "abc:def", "abc*def", "abc?def", "abc\"def", "abc<def", "abc>def", "abc|def"
    })
    void debeRechazarSegmentosPeligrosos(String value) {
        assertThatThrownBy(() -> storagePathSanitizer.sanitizeSegment(value))
                .isInstanceOf(InvalidStoragePathException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { "CON", "CON.txt", "NUL", "COM1", "LPT9", "CLOCK$", "abc.", "abc " })
    void debeRechazarSegmentosInvalidosEnWindows(String value) {
        assertThatThrownBy(() -> storagePathSanitizer.sanitizeSegment(value))
                .isInstanceOf(InvalidStoragePathException.class);
    }
}
