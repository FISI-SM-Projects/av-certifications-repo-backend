package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CoursePayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.IssuerPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.TeacherPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CourseCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.pdf.PdfBoxPdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.FileSystemConstanciaRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CourseCertificateRequestValidator;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.StoragePathSanitizer;

class CourseCertificateIntegrationTest {

    @TempDir
    private Path tempDir;

    private CourseCertificateService service;
    private FileSystemConstanciaRepository repository;

    @BeforeEach
    void setUp() {
        StoragePathSanitizer sanitizer = new StoragePathSanitizer();
        repository = new FileSystemConstanciaRepository(tempDir, new ObjectMapper().findAndRegisterModules(),
                sanitizer);
        service = new CourseCertificateService(
                new CourseCertificateRequestValidator(),
                new CertificateIdService(sanitizer),
                repository,
                new PdfBoxPdfGenerationService(),
                Clock.fixed(Instant.parse("2026-07-14T10:30:00Z"), ZoneId.of("UTC")));
    }

    @Test
    void flujoCompletoDebePersistirDosVersionesEnCarpetaTemporal() throws Exception {
        CourseCertificateResponse first = service.generateCourseCertificate(validRequest());
        CourseCertificateResponse second = service.generateCourseCertificate(validRequest());

        assertThat(first.getGenerationId()).isEqualTo("22200275-32BGNYGF-1-26.1-v001");
        assertThat(second.getGenerationId()).isEqualTo("22200275-32BGNYGF-1-26.1-v002");

        Path baseDirectory = tempDir.resolve("certificates")
                .resolve("course")
                .resolve("26.1")
                .resolve("22200275")
                .resolve("32BGNYGF-1");
        Path versionOne = baseDirectory.resolve("v001");
        Path versionTwo = baseDirectory.resolve("v002");

        assertThat(versionOne.resolve("request.json")).exists();
        assertThat(versionOne.resolve("metadata.json")).exists();
        assertThat(versionOne.resolve("certificate.pdf")).exists();
        assertThat(Files.size(versionOne.resolve("certificate.pdf"))).isGreaterThan(1_000);
        assertThat(versionTwo.resolve("request.json")).exists();
        assertThat(versionTwo.resolve("metadata.json")).exists();
        assertThat(versionTwo.resolve("certificate.pdf")).exists();
        assertThat(Files.size(versionTwo.resolve("certificate.pdf"))).isGreaterThan(1_000);

        List<CertificateGenerationMetadata> history =
                repository.findHistoryByCertificateKey("22200275-32BGNYGF-1-26.1");
        assertThat(history).extracting(CertificateGenerationMetadata::getGenerationId)
                .containsExactly("22200275-32BGNYGF-1-26.1-v001", "22200275-32BGNYGF-1-26.1-v002");

        assertThat(repository.readPdf(first.getGenerationId())).isPresent();
        assertThat(repository.readPdf(second.getGenerationId())).isPresent();
        assertThat(tempDir.resolve("certificates")).exists();
    }

    private CourseCertificateRequest validRequest() {
        return new CourseCertificateRequest(
                new TeacherPayload("Jos\u00e9 Mu\u00f1oz Pe\u00f1a", "jmunoz@unmsm.edu.pe", "22200275"),
                new CoursePayload("32BGNYGF", "Nombre del curso", "7", "1", "SW", "2023", "26.1"),
                new IssuerPayload("moodle", "12345", "usuario@unmsm.edu.pe"));
    }
}
