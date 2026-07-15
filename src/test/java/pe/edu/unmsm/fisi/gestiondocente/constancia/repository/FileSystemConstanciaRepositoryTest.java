package pe.edu.unmsm.fisi.gestiondocente.constancia.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.GenerationAlreadyExistsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.StorageException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.StoragePathSanitizer;

class FileSystemConstanciaRepositoryTest {

    @TempDir
    private Path tempDir;

    private FileSystemConstanciaRepository repository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        repository = new FileSystemConstanciaRepository(tempDir, objectMapper, new StoragePathSanitizer());
    }

    @Test
    void debeCrearCarpetasYGuardarRequestMetadataYPdf() throws Exception {
        CertificateGenerationMetadata metadata = courseMetadata(1, EstadoConstancia.GENERADO);
        byte[] pdfBytes = new byte[] { 1, 2, 3 };

        repository.saveGeneration(request(), metadata, pdfBytes);

        Path generationDirectory = tempDir.resolve("certificates")
                .resolve("course")
                .resolve("26.1")
                .resolve("22200275")
                .resolve("32BGNYGF-1")
                .resolve("v001");

        assertThat(Files.exists(generationDirectory.resolve("request.json"))).isTrue();
        assertThat(Files.exists(generationDirectory.resolve("metadata.json"))).isTrue();
        assertThat(Files.readAllBytes(generationDirectory.resolve("certificate.pdf"))).isEqualTo(pdfBytes);
        assertThat(Files.readString(generationDirectory.resolve("request.json"))).contains("moodle");

        CertificateGenerationMetadata storedMetadata = objectMapper.readValue(
                generationDirectory.resolve("metadata.json").toFile(), CertificateGenerationMetadata.class);
        assertThat(storedMetadata.getGenerationId()).isEqualTo(metadata.getGenerationId());
    }

    @Test
    void debePermitirGuardarGeneracionSinPdf() {
        CertificateGenerationMetadata metadata = courseMetadata(1, EstadoConstancia.GENERADO);

        repository.saveGeneration(request(), metadata, null);

        assertThat(repository.readPdf(metadata.getGenerationId())).isEmpty();
    }

    @Test
    void primeraVersionDebeSerUnoYSegundaDebeSerDos() {
        String certificateKey = "22200275-32BGNYGF-1-26.1";

        assertThat(repository.nextVersion(certificateKey)).isEqualTo(1);

        repository.saveGeneration(request(), courseMetadata(1, EstadoConstancia.GENERADO), null);

        assertThat(repository.nextVersion(certificateKey)).isEqualTo(2);
    }

    @Test
    void noDebeSobrescribirGeneracionExistente() {
        CertificateGenerationMetadata metadata = courseMetadata(1, EstadoConstancia.GENERADO);
        repository.saveGeneration(request(), metadata, null);

        assertThatThrownBy(() -> repository.saveGeneration(request(), metadata, null))
                .isInstanceOf(GenerationAlreadyExistsException.class);
    }

    @Test
    void nextVersionDebeUsarMaximoMasUnoAunqueHayaHuecos() {
        String certificateKey = "22200275-32BGNYGF-1-26.1";
        repository.saveGeneration(request(), courseMetadata(1, EstadoConstancia.GENERADO), null);
        repository.saveGeneration(request(), courseMetadata(3, EstadoConstancia.GENERADO), null);

        assertThat(repository.nextVersion(certificateKey)).isEqualTo(4);
    }

    @Test
    void debeBuscarPorGenerationId() {
        CertificateGenerationMetadata metadata = courseMetadata(1, EstadoConstancia.GENERADO);
        repository.saveGeneration(request(), metadata, null);

        Optional<CertificateGenerationMetadata> found = repository.findByGenerationId(metadata.getGenerationId());

        assertThat(found).isPresent();
        assertThat(found.get().getCertificateKey()).isEqualTo(metadata.getCertificateKey());
    }

    @Test
    void historialDebeEstarOrdenadoPorVersion() {
        String certificateKey = "22200275-32BGNYGF-1-26.1";
        repository.saveGeneration(request(), courseMetadata(2, EstadoConstancia.GENERADO), null);
        repository.saveGeneration(request(), courseMetadata(1, EstadoConstancia.GENERADO), null);

        List<CertificateGenerationMetadata> history = repository.findHistoryByCertificateKey(certificateKey);

        assertThat(history).extracting(CertificateGenerationMetadata::getVersion)
                .containsExactly(1, 2);
    }

    @Test
    void latestDebeDevolverMayorVersion() {
        String certificateKey = "22200275-32BGNYGF-1-26.1";
        repository.saveGeneration(request(), courseMetadata(1, EstadoConstancia.GENERADO), null);
        repository.saveGeneration(request(), courseMetadata(2, EstadoConstancia.GENERADO), null);

        Optional<CertificateGenerationMetadata> latest = repository.findLatestByCertificateKey(certificateKey);

        assertThat(latest).isPresent();
        assertThat(latest.get().getVersion()).isEqualTo(2);
    }

    @Test
    void listadoPorDocenteDebeDevolverSoloUltimaVersionPorClave() {
        repository.saveGeneration(request(), courseMetadata(1, EstadoConstancia.GENERADO), null);
        repository.saveGeneration(request(), courseMetadata(2, EstadoConstancia.GENERADO), null);
        repository.saveGeneration(request(), semesterMetadata(1, "22200275", "26.1", EstadoConstancia.GENERADO), null);

        List<CertificateGenerationMetadata> latestByTeacher = repository.findLatestByTeacherCode("22200275");

        assertThat(latestByTeacher).hasSize(2);
        assertThat(latestByTeacher).extracting(CertificateGenerationMetadata::getGenerationId)
                .contains("22200275-32BGNYGF-1-26.1-v002", "22200275-26.1-v001");
    }

    @Test
    void filtroPorDocenteYSemestreNoDebeMezclarDatos() {
        repository.saveGeneration(request(), courseMetadata(1, EstadoConstancia.GENERADO), null);
        repository.saveGeneration(request(), courseMetadataFor("33300333", "32BGNYGF", "1", "26.1", 1), null);
        repository.saveGeneration(request(), courseMetadataFor("22200275", "32BGNYGF", "1", "26.2", 1), null);
        repository.saveGeneration(request(), semesterMetadata(1, "22200275", "26.1", EstadoConstancia.GENERADO), null);

        List<CertificateGenerationMetadata> result = repository.findByTeacherCodeAndSemester("22200275", "26.1");

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(metadata -> {
            assertThat(metadata.getTeacherCode()).isEqualTo("22200275");
            assertThat(metadata.getSemester()).isEqualTo("26.1");
        });
    }

    @Test
    void cursoYSemestralPuedenCoexistir() {
        CertificateGenerationMetadata course = courseMetadata(1, EstadoConstancia.GENERADO);
        CertificateGenerationMetadata semester = semesterMetadata(1, "22200275", "26.1", EstadoConstancia.GENERADO);

        repository.saveGeneration(request(), course, null);
        repository.saveGeneration(request(), semester, null);

        assertThat(repository.findByGenerationId(course.getGenerationId())).isPresent();
        assertThat(repository.findByGenerationId(semester.getGenerationId())).isPresent();
    }

    @Test
    void existsApprovedDebeSerFalseConSoloGeneradoYTrueConAprobado() {
        String certificateKey = "22200275-32BGNYGF-1-26.1";
        repository.saveGeneration(request(), courseMetadata(1, EstadoConstancia.GENERADO), null);

        assertThat(repository.existsApprovedByCertificateKey(certificateKey)).isFalse();

        repository.saveGeneration(request(), courseMetadata(2, EstadoConstancia.APROBADO), null);

        assertThat(repository.existsApprovedByCertificateKey(certificateKey)).isTrue();
    }

    @Test
    void readPdfDebeDevolverBytesCuandoExisten() {
        CertificateGenerationMetadata metadata = courseMetadata(1, EstadoConstancia.GENERADO);
        byte[] pdfBytes = new byte[] { 9, 8, 7 };
        repository.saveGeneration(request(), metadata, pdfBytes);

        Optional<byte[]> storedPdf = repository.readPdf(metadata.getGenerationId());

        assertThat(storedPdf).isPresent();
        assertThat(storedPdf.get()).isEqualTo(pdfBytes);
    }

    @Test
    void operacionesDebenRechazarSegmentosPeligrosos() {
        List<String> dangerousValues = List.of("../", "..", "abc/def", "abc\\def", "C:\\temp\\x", "abc\u0000def");

        for (String value : dangerousValues) {
            CertificateGenerationMetadata metadata = courseMetadata(1, EstadoConstancia.GENERADO);
            metadata.setTeacherCode(value);
            assertThatThrownBy(() -> repository.saveGeneration(request(), metadata, null))
                    .isInstanceOf(StorageException.class);
        }
    }

    @Test
    void falloDuranteEscrituraNoDebeDejarGeneracionVisibleNiTemporal() throws Exception {
        CertificateGenerationMetadata metadata = courseMetadata(1, EstadoConstancia.GENERADO);
        Object brokenRequest = new Object() {
            public String getBroken() {
                throw new IllegalStateException("boom");
            }
        };

        assertThatThrownBy(() -> repository.saveGeneration(brokenRequest, metadata, null))
                .isInstanceOf(StorageException.class);

        assertThat(repository.findByGenerationId(metadata.getGenerationId())).isEmpty();
        try (java.util.stream.Stream<Path> paths = Files.walk(tempDir)) {
            assertThat(paths.map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith(".tmp-"))
                    .toList()).isEmpty();
        }
    }

    @Test
    void ningunaOperacionDebeCrearArchivosFueraDeTempDir() {
        CertificateGenerationMetadata metadata = courseMetadata(1, EstadoConstancia.GENERADO);

        assertThatCode(() -> repository.saveGeneration(request(), metadata, null)).doesNotThrowAnyException();

        assertThat(tempDir.resolve("certificates")).exists();
    }

    private Map<String, String> request() {
        return Map.of("system", "moodle", "teacher_code", "22200275");
    }

    private CertificateGenerationMetadata courseMetadata(int version, EstadoConstancia status) {
        return courseMetadataFor("22200275", "32BGNYGF", "1", "26.1", version, status);
    }

    private CertificateGenerationMetadata courseMetadataFor(String teacherCode, String courseCode, String section,
            String semester, int version) {
        return courseMetadataFor(teacherCode, courseCode, section, semester, version, EstadoConstancia.GENERADO);
    }

    private CertificateGenerationMetadata courseMetadataFor(String teacherCode, String courseCode, String section,
            String semester, int version, EstadoConstancia status) {
        String certificateKey = teacherCode + "-" + courseCode + "-" + section + "-" + semester;

        return new CertificateGenerationMetadata(
                certificateKey + "-v" + String.format("%03d", version),
                certificateKey,
                version,
                TipoConstancia.CURSO,
                status,
                teacherCode,
                courseCode,
                section,
                semester,
                LocalDateTime.of(2026, 7, 14, 12, 0),
                "request.json",
                "certificate.pdf");
    }

    private CertificateGenerationMetadata semesterMetadata(int version, String teacherCode, String semester,
            EstadoConstancia status) {
        String certificateKey = teacherCode + "-" + semester;

        return new CertificateGenerationMetadata(
                certificateKey + "-v" + String.format("%03d", version),
                certificateKey,
                version,
                TipoConstancia.SEMESTRAL,
                status,
                teacherCode,
                null,
                null,
                semester,
                LocalDateTime.of(2026, 7, 14, 12, 0),
                "request.json",
                "certificate.pdf");
    }
}
