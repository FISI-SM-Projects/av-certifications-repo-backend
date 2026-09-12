package pe.edu.unmsm.fisi.gestiondocente.shared.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ApiEndpointArchitectureTest {
    private static final List<String> LEGACY_PREFIXES = List.of(
            "/api/v1/docentes",
            "/api/v1/constancias",
            "/api/v1/director",
            "/api/v1/auth/me"
    );
    private static final List<String> INFRASTRUCTURE_PREFIXES = List.of("/api/v1/health");
    private static final List<String> FORBIDDEN_SPANISH_SEGMENTS = List.of(
            "docentes", "constancias", "carga-academica", "firma", "semestral", "curso"
    );
    private static final Pattern MAPPING_VALUE = Pattern.compile("@(?:Request|Get|Post|Put|Patch|Delete)Mapping\\(\\s*\"([^\"]+)\"");

    @Test
    void newPublicApiRoutesMustUseEnglishSegments() throws IOException {
        Path sourceRoot = Path.of("src/main/java");
        List<String> violations;
        try (var files = Files.walk(sourceRoot)) {
            violations = files.filter(path -> path.toString().endsWith("Controller.java"))
                    .flatMap(path -> mappingValues(path).stream().map(value -> path + " -> " + value))
                    .filter(mapping -> !isLegacyOrInfrastructure(mapping))
                    .filter(ApiEndpointArchitectureTest::containsForbiddenSpanishSegment)
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
                values.add(value.startsWith("/api/v1") || value.equals(basePath) ? value : basePath + value);
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

    private static boolean isLegacyOrInfrastructure(String mapping) {
        return LEGACY_PREFIXES.stream().anyMatch(mapping::contains)
                || INFRASTRUCTURE_PREFIXES.stream().anyMatch(mapping::contains);
    }

    private static boolean containsForbiddenSpanishSegment(String mapping) {
        return FORBIDDEN_SPANISH_SEGMENTS.stream().anyMatch(segment -> mapping.contains("/" + segment));
    }
}
