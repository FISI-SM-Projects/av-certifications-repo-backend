package pe.edu.unmsm.fisi.gestiondocente.shared.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ApiEndpointArchitectureTest {
    private static final List<String> ALLOWED_PREFIXES = List.of(
            "/auth",
            "/teachers",
            "/certificates",
            "/health"
    );
    private static final List<String> FORBIDDEN_SEGMENTS = List.of(
            "api", "api/v1", "docentes", "constancias", "director", "carga-academica", "firma", "semestral", "curso",
            "generaciones", "certificados", "historial", "download", "demo", "legacy", "temp"
    );
    private static final Pattern MAPPING_VALUE = Pattern.compile("@(?:Request|Get|Post|Put|Patch|Delete)Mapping\\(\\s*\"([^\"]+)\"");

    @Test
    void publicApiRoutesMustUseFinalEnglishContract() throws IOException {
        Path sourceRoot = Path.of("src/main/java");
        List<String> violations;
        try (var files = Files.walk(sourceRoot)) {
            violations = files.filter(path -> path.toString().endsWith("Controller.java"))
                    .flatMap(path -> mappingValues(path).stream()
                            .filter(mapping -> !isAllowed(mapping))
                            .map(value -> path + " -> " + value))
                    .toList();
        }

        assertThat(violations).isEmpty();
    }

    private static List<String> mappingValues(Path path) {
        try {
            String source = Files.readString(path);
            String basePath = classBasePath(source);
            var matcher = MAPPING_VALUE.matcher(source);
            var values = new java.util.ArrayList<String>();
            while (matcher.find()) {
                String value = matcher.group(1);
                values.add(value.equals(basePath) ? value : normalize(basePath, value));
            }
            return values;
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer " + path, exception);
        }
    }

    private static String classBasePath(String source) {
        var matcher = MAPPING_VALUE.matcher(source);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static boolean isAllowed(String mapping) {
        if (FORBIDDEN_SEGMENTS.stream().anyMatch(segment -> mapping.contains("/" + segment))) {
            return false;
        }
        return ALLOWED_PREFIXES.stream().anyMatch(mapping::startsWith);
    }

    private static String normalize(String basePath, String value) {
        String base = basePath == null ? "" : basePath;
        String path = value == null ? "" : value;
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + path;
    }
}
