package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import pe.edu.unmsm.fisi.gestiondocente.constancia.controller.ConstanciaExceptionHandler;
import pe.edu.unmsm.fisi.gestiondocente.constancia.controller.ConstanciaQueryController;
import pe.edu.unmsm.fisi.gestiondocente.constancia.controller.CourseCertificateController;
import pe.edu.unmsm.fisi.gestiondocente.constancia.controller.SemesterCertificateController;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CoursePayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.IssuerPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.TeacherPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CourseCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.pdf.PdfBoxPdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.FileSystemConstanciaRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CourseCertificateRequestValidator;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.SemesterCertificateRequestValidator;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.StoragePathSanitizer;

class SemesterCertificateIntegrationTest {

    @TempDir
    private Path tempDir;

    private ObjectMapper objectMapper;
    private FileSystemConstanciaRepository repository;
    private CourseCertificateService courseService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        StoragePathSanitizer sanitizer = new StoragePathSanitizer();
        repository = new FileSystemConstanciaRepository(tempDir, objectMapper, sanitizer);
        CertificateIdService certificateIdService = new CertificateIdService(sanitizer);
        PdfBoxPdfGenerationService pdfService = new PdfBoxPdfGenerationService();
        Clock fixedClock = Clock.fixed(Instant.parse("2026-07-14T10:30:00Z"), ZoneId.of("UTC"));
        courseService = new CourseCertificateService(
                new CourseCertificateRequestValidator(),
                certificateIdService,
                repository,
                pdfService,
                fixedClock);
        SemesterCertificateService semesterService = new SemesterCertificateService(
                new SemesterCertificateRequestValidator(),
                certificateIdService,
                repository,
                pdfService,
                fixedClock);
        ConstanciaQueryService queryService = new ConstanciaQueryService(repository);

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new CourseCertificateController(courseService),
                        new SemesterCertificateController(semesterService),
                        new ConstanciaQueryController(queryService))
                .setControllerAdvice(new ConstanciaExceptionHandler())
                .setMessageConverters(new ByteArrayHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void endpointSemestralDebeGenerarPersistirConsultarYDescargarPdf() throws Exception {
        CourseCertificateResponse courseOneV1 = courseService.generateCourseCertificate(
                courseRequest("32BGNYGF", "Ingenieria y Gestion de Proyectos", "1", "SW", "2023", "22200275"));
        CourseCertificateResponse courseOneV2 = courseService.generateCourseCertificate(
                courseRequest("32BGNYGF", "Ingenieria y Gestion de Proyectos", "1", "SW", "2023", "22200275"));
        CourseCertificateResponse courseTwo = courseService.generateCourseCertificate(
                courseRequest("32SW001", "Arquitectura de Software", "2", "SW", "2023", "22200275"));

        mockMvc.perform(post("/api/v1/constancias/semestral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(semesterJson("32BGNYGF", "1", "32SW001", "2")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generationId").value("22200275-26.1-v001"))
                .andExpect(jsonPath("$.certificateKey").value("22200275-26.1"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.type").value("SEMESTRAL"))
                .andExpect(jsonPath("$.status").value("GENERADO"))
                .andExpect(jsonPath("$.teacherCode").value("22200275"))
                .andExpect(jsonPath("$.teacherFullName").value("Jos\u00e9 Mu\u00f1oz Pe\u00f1a"))
                .andExpect(jsonPath("$.courseCount").value(2))
                .andExpect(jsonPath("$.sourceGenerationIds[0]").value(courseOneV2.getGenerationId()))
                .andExpect(jsonPath("$.sourceGenerationIds[1]").value(courseTwo.getGenerationId()))
                .andExpect(jsonPath("$.viewUrl").value(
                        "/api/v1/constancias/generaciones/22200275-26.1-v001/pdf"));

        assertThat(courseOneV1.getGenerationId()).endsWith("v001");
        Path semesterDirectory = tempDir.resolve("certificates")
                .resolve("semester")
                .resolve("26.1")
                .resolve("22200275")
                .resolve("v001");
        assertThat(semesterDirectory.resolve("source-summary.json")).exists();
        assertThat(semesterDirectory.resolve("metadata.json")).exists();
        assertThat(semesterDirectory.resolve("certificate.pdf")).exists();
        assertThat(Files.readString(semesterDirectory.resolve("source-summary.json")))
                .contains(courseOneV2.getGenerationId())
                .contains(courseTwo.getGenerationId())
                .contains("Arquitectura de Software");
        assertThat(Files.readAllBytes(semesterDirectory.resolve("certificate.pdf")))
                .startsWith("%PDF-".getBytes(StandardCharsets.US_ASCII));

        byte[] pdfBytes = repository.readPdf("22200275-26.1-v001").orElseThrow();
        String pdfText = extractText(pdfBytes);
        assertThat(pdfText)
                .contains("CONSTANCIA SEMESTRAL")
                .contains("Jos\u00e9 Mu\u00f1oz Pe\u00f1a")
                .contains("26.1")
                .contains("32BGNYGF")
                .contains("32SW001")
                .contains("22200275-26.1-v001");

        mockMvc.perform(get("/api/v1/constancias/docentes/22200275"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.generationId=='22200275-26.1-v001')].type").value("SEMESTRAL"));
        assertThat(repository.findByGenerationId("22200275-26.1-v001").orElseThrow().getCourseCode()).isNull();

        mockMvc.perform(get("/api/v1/constancias/generaciones/22200275-26.1-v001/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("inline")))
                .andExpect(content().bytes(pdfBytes));

        mockMvc.perform(get("/api/v1/constancias/generaciones/22200275-26.1-v001/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, not(containsString("/"))))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, not(containsString("\\"))));
    }

    @Test
    void cursoFaltanteDebeResponder409YNoCrearGeneracionSemestral() throws Exception {
        courseService.generateCourseCertificate(
                courseRequest("32BGNYGF", "Ingenieria y Gestion de Proyectos", "1", "SW", "2023", "22200275"));

        mockMvc.perform(post("/api/v1/constancias/semestral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(semesterJson("32BGNYGF", "1", "32SW001", "2")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.missingCourses[0].code").value("32SW001"))
                .andExpect(jsonPath("$.missingCourses[0].section").value("2"));

        assertThat(tempDir.resolve("certificates").resolve("semester")).doesNotExist();
    }

    @Test
    void otroDocenteSemestreOSeccionNoDebeContarComoFuente() throws Exception {
        courseService.generateCourseCertificate(
                courseRequest("32SW001", "Arquitectura de Software", "2", "SW", "2023", "22200999"));
        courseService.generateCourseCertificate(
                courseRequest("32SW001", "Arquitectura de Software", "3", "SW", "2023", "22200275"));
        courseService.generateCourseCertificate(
                courseRequest("32SW001", "Arquitectura de Software", "2", "SW", "2023", "22200275", "26.2"));

        mockMvc.perform(post("/api/v1/constancias/semestral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teacher_code": "22200275",
                                  "semester": "26.1",
                                  "expected_courses": [
                                    { "code": "32SW001", "section": "2" }
                                  ]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.missingCourses[0].code").value("32SW001"));
    }

    @Test
    void segundaSemestralDebeCrearV002SinSobrescribirV001() throws Exception {
        courseService.generateCourseCertificate(
                courseRequest("32BGNYGF", "Ingenieria y Gestion de Proyectos", "1", "SW", "2023", "22200275"));

        mockMvc.perform(post("/api/v1/constancias/semestral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(singleSemesterJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generationId").value("22200275-26.1-v001"));
        mockMvc.perform(post("/api/v1/constancias/semestral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(singleSemesterJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generationId").value("22200275-26.1-v002"));

        Path base = tempDir.resolve("certificates").resolve("semester").resolve("26.1").resolve("22200275");
        assertThat(base.resolve("v001").resolve("source-summary.json")).exists();
        assertThat(base.resolve("v002").resolve("source-summary.json")).exists();

        mockMvc.perform(get("/api/v1/constancias/certificados/22200275-26.1/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].generationId").value("22200275-26.1-v001"))
                .andExpect(jsonPath("$[1].generationId").value("22200275-26.1-v002"));
    }

    @Test
    void semestralAprobadaDebeBloquearNuevaGeneracion() throws Exception {
        courseService.generateCourseCertificate(
                courseRequest("32BGNYGF", "Ingenieria y Gestion de Proyectos", "1", "SW", "2023", "22200275"));
        CertificateGenerationMetadata approved = new CertificateGenerationMetadata(
                "22200275-26.1-v001",
                "22200275-26.1",
                1,
                TipoConstancia.SEMESTRAL,
                EstadoConstancia.APROBADO,
                "22200275",
                null,
                null,
                "26.1",
                java.time.LocalDateTime.of(2026, 7, 14, 10, 30),
                "source-summary.json",
                "certificate.pdf");
        repository.saveGeneration(java.util.Map.of("sourceGenerations", java.util.List.of()), approved,
                "%PDF-approved".getBytes(StandardCharsets.US_ASCII));

        mockMvc.perform(post("/api/v1/constancias/semestral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(singleSemesterJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "La constancia ya fue aprobada y no admite nuevas generaciones"));

        assertThat(repository.findHistoryByCertificateKey("22200275-26.1")).hasSize(1);
    }

    private CourseCertificateRequest courseRequest(String code, String subject, String section, String school,
            String plan, String teacherCode) {
        return courseRequest(code, subject, section, school, plan, teacherCode, "26.1");
    }

    private CourseCertificateRequest courseRequest(String code, String subject, String section, String school,
            String plan, String teacherCode, String semester) {
        TeacherPayload teacher = "22200999".equals(teacherCode)
                ? new TeacherPayload("Ana Torres Lima", "atorres@unmsm.edu.pe", teacherCode)
                : new TeacherPayload("Jos\u00e9 Mu\u00f1oz Pe\u00f1a", "jmunoz@unmsm.edu.pe", teacherCode);
        return new CourseCertificateRequest(
                teacher,
                new CoursePayload(code, subject, "7", section, school, plan, semester),
                new IssuerPayload("moodle", "12345", "usuario@unmsm.edu.pe"));
    }

    private String semesterJson(String firstCode, String firstSection, String secondCode, String secondSection) {
        return """
                {
                  "teacher_code": "22200275",
                  "semester": "26.1",
                  "expected_courses": [
                    { "code": "%s", "section": "%s" },
                    { "code": "%s", "section": "%s" }
                  ]
                }
                """.formatted(firstCode, firstSection, secondCode, secondSection);
    }

    private String singleSemesterJson() {
        return """
                {
                  "teacher_code": "22200275",
                  "semester": "26.1",
                  "expected_courses": [
                    { "code": "32BGNYGF", "section": "1" }
                  ]
                }
                """;
    }

    private String extractText(byte[] pdfBytes) throws Exception {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
