package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import static org.assertj.core.api.Assertions.assertThat;

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
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CertificateGenerationResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CourseCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.pdf.PdfBoxPdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.FileSystemConstanciaRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CourseCertificateRequestValidator;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.StoragePathSanitizer;

class ConstanciaQueryIntegrationTest {

    @TempDir
    private Path tempDir;

    private CourseCertificateService courseCertificateService;
    private ConstanciaQueryService queryService;

    @BeforeEach
    void setUp() {
        StoragePathSanitizer sanitizer = new StoragePathSanitizer();
        FileSystemConstanciaRepository repository = new FileSystemConstanciaRepository(
                tempDir, new ObjectMapper().findAndRegisterModules(), sanitizer);
        courseCertificateService = new CourseCertificateService(
                new CourseCertificateRequestValidator(),
                new CertificateIdService(sanitizer),
                repository,
                new PdfBoxPdfGenerationService(),
                Clock.fixed(Instant.parse("2026-07-14T10:30:00Z"), ZoneId.of("UTC")));
        queryService = new ConstanciaQueryService(repository);
    }

    @Test
    void consultasDebenLeerGeneracionesPersistidasEnTempDir() {
        CourseCertificateResponse first = courseCertificateService.generateCourseCertificate(validRequest());
        CourseCertificateResponse second = courseCertificateService.generateCourseCertificate(validRequest());

        List<CertificateGenerationResponse> latest = queryService.listLatestByTeacherCode("22200275");
        List<CertificateGenerationResponse> history =
                queryService.findHistoryByCertificateKey("22200275-32BGNYGF-1-26.1");
        CertificateGenerationResponse generation = queryService.findByGenerationId(first.getGenerationId());
        byte[] pdf = queryService.readPdf(second.getGenerationId());

        assertThat(latest).hasSize(1);
        assertThat(latest.get(0).getGenerationId()).isEqualTo("22200275-32BGNYGF-1-26.1-v002");
        assertThat(history).extracting(CertificateGenerationResponse::getGenerationId)
                .containsExactly("22200275-32BGNYGF-1-26.1-v001", "22200275-32BGNYGF-1-26.1-v002");
        assertThat(generation.getVersion()).isEqualTo(1);
        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(tempDir.resolve("certificates")).exists();
    }

    private CourseCertificateRequest validRequest() {
        return new CourseCertificateRequest(
                new TeacherPayload("Jos\u00e9 Mu\u00f1oz Pe\u00f1a", "jmunoz@unmsm.edu.pe", "22200275"),
                new CoursePayload("32BGNYGF", "Nombre del curso", "7", "1", "SW", "2023", "26.1"),
                new IssuerPayload("moodle", "12345", "usuario@unmsm.edu.pe"));
    }
}
