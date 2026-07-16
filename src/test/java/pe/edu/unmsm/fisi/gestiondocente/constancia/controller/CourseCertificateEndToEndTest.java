package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.PdfGenerationException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.StorageException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.pdf.PdfBoxPdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.pdf.PdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.CertificateGenerationRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.FileSystemConstanciaRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.CertificateIdService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.ConstanciaQueryService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.CourseCertificateService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CourseCertificateRequestValidator;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.StoragePathSanitizer;

class CourseCertificateEndToEndTest {

    @TempDir
    private Path tempDir;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private FileSystemConstanciaRepository repository;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        StoragePathSanitizer sanitizer = new StoragePathSanitizer();
        repository = new FileSystemConstanciaRepository(tempDir, objectMapper, sanitizer);
        CourseCertificateService courseService = new CourseCertificateService(
                new CourseCertificateRequestValidator(),
                new CertificateIdService(sanitizer),
                repository,
                new PdfBoxPdfGenerationService(),
                Clock.fixed(Instant.parse("2026-07-14T10:30:00Z"), ZoneId.of("UTC")));
        ConstanciaQueryService queryService = new ConstanciaQueryService(repository);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CourseCertificateController(courseService),
                        new ConstanciaQueryController(queryService))
                .setControllerAdvice(new ConstanciaExceptionHandler())
                .setMessageConverters(new ByteArrayHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void flujoCompletoDebeGenerarPersistirVersionarConsultarYEntregarPdf() throws Exception {
        MvcResult firstResult = postValidCertificate(validJson())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.certificateKey").value("22200275-32BGNYGF-1-26.1"))
                .andExpect(jsonPath("$.generationId").value("22200275-32BGNYGF-1-26.1-v001"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.type").value("CURSO"))
                .andExpect(jsonPath("$.status").value("GENERADO"))
                .andExpect(jsonPath("$.generatedAt").value("2026-07-14T10:30:00Z"))
                .andExpect(jsonPath("$.viewUrl").value(
                        "/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/pdf"))
                .andExpect(jsonPath("$.downloadUrl").value(
                        "/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/download"))
                .andReturn();

        JsonNode firstResponse = objectMapper.readTree(firstResult.getResponse().getContentAsString());
        assertThat(firstResponse.get("generationId").asText()).isEqualTo("22200275-32BGNYGF-1-26.1-v001");
        Path v001 = generationDirectory(1);
        assertStoredGeneration(v001, "22200275-32BGNYGF-1-26.1-v001", 1);

        byte[] v001PdfBeforeSecondGeneration = Files.readAllBytes(v001.resolve("certificate.pdf"));
        String v001RequestBeforeSecondGeneration = Files.readString(v001.resolve("request.json"));
        String v001MetadataBeforeSecondGeneration = Files.readString(v001.resolve("metadata.json"));

        postValidCertificate(validJson())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generationId").value("22200275-32BGNYGF-1-26.1-v002"))
                .andExpect(jsonPath("$.version").value(2));

        Path v002 = generationDirectory(2);
        assertStoredGeneration(v002, "22200275-32BGNYGF-1-26.1-v002", 2);
        assertThat(v001).exists();
        assertThat(Files.readAllBytes(v001.resolve("certificate.pdf"))).isEqualTo(v001PdfBeforeSecondGeneration);
        assertThat(Files.readString(v001.resolve("request.json"))).isEqualTo(v001RequestBeforeSecondGeneration);
        assertThat(Files.readString(v001.resolve("metadata.json"))).isEqualTo(v001MetadataBeforeSecondGeneration);

        mockMvc.perform(get("/api/v1/constancias/docentes/22200275"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].generationId").value("22200275-32BGNYGF-1-26.1-v002"))
                .andExpect(content().string(not(containsString("22200275-32BGNYGF-1-26.1-v001\",\""))))
                .andExpect(content().string(not(containsString("requestFile"))))
                .andExpect(content().string(not(containsString("pdfFile"))))
                .andExpect(content().string(not(containsString(tempDir.toString()))));

        mockMvc.perform(get("/api/v1/constancias/certificados/22200275-32BGNYGF-1-26.1/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].generationId").value("22200275-32BGNYGF-1-26.1-v001"))
                .andExpect(jsonPath("$[0].version").value(1))
                .andExpect(jsonPath("$[1].generationId").value("22200275-32BGNYGF-1-26.1-v002"))
                .andExpect(jsonPath("$[1].version").value(2));

        mockMvc.perform(get("/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generationId").value("22200275-32BGNYGF-1-26.1-v001"))
                .andExpect(jsonPath("$.certificateKey").value("22200275-32BGNYGF-1-26.1"))
                .andExpect(content().string(not(containsString("requestFile"))))
                .andExpect(content().string(not(containsString("pdfFile"))));

        byte[] storedPdf = Files.readAllBytes(v002.resolve("certificate.pdf"));
        MvcResult inlinePdf = mockMvc.perform(get("/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v002/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("inline")))
                .andReturn();
        assertThat(inlinePdf.getResponse().getContentAsByteArray()).isEqualTo(storedPdf);

        MvcResult downloadedPdf = mockMvc.perform(
                        get("/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v002/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".pdf")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, not(containsString("/"))))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, not(containsString("\\"))))
                .andReturn();
        assertThat(downloadedPdf.getResponse().getContentAsByteArray())
                .isEqualTo(inlinePdf.getResponse().getContentAsByteArray());
    }

    @Test
    void listadosEHistorialesDebenSepararDocentes() throws Exception {
        postValidCertificate(validJson()).andExpect(status().isCreated());
        postValidCertificate(validJsonForOtherTeacher()).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/constancias/docentes/22200275"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("22200275-32BGNYGF-1-26.1-v001")))
                .andExpect(content().string(not(containsString("22200999-32BGNYGF-1-26.1-v001"))));

        mockMvc.perform(get("/api/v1/constancias/docentes/22200999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("22200999-32BGNYGF-1-26.1-v001")))
                .andExpect(content().string(not(containsString("22200275-32BGNYGF-1-26.1-v001"))));

        mockMvc.perform(get("/api/v1/constancias/certificados/22200275-32BGNYGF-1-26.1/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].teacherCode").value("22200275"));
    }

    @Test
    void validacionesNoDebenCrearGeneracion() throws Exception {
        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teacher": {
                                    "full_name": "José Muñoz Peña",
                                    "email": null,
                                    "teacher_code": ""
                                  },
                                  "course": {
                                    "code": "",
                                    "subject": "Ingeniería y Gestión de Proyectos",
                                    "cycle": "7",
                                    "section": "   ",
                                    "school": "SW",
                                    "plan": "2023",
                                    "semester": null
                                  },
                                  "issuer": {
                                    "system": "moodle",
                                    "executed_by_userid": "12345",
                                    "executed_by_email": ""
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.missingFields.length()").value(6))
                .andExpect(jsonPath("$.missingFields[0]").value("teacher.email"))
                .andExpect(jsonPath("$.missingFields[1]").value("teacher.teacher_code"))
                .andExpect(jsonPath("$.missingFields[2]").value("course.code"))
                .andExpect(jsonPath("$.missingFields[3]").value("course.section"))
                .andExpect(jsonPath("$.missingFields[4]").value("course.semester"))
                .andExpect(jsonPath("$.missingFields[5]").value("issuer.executed_by_email"));

        assertThat(tempDir.resolve("certificates")).doesNotExist();
    }

    @Test
    void objetosPadreAusentesDebenReportarseJuntos() throws Exception {
        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.missingFields.length()").value(3))
                .andExpect(jsonPath("$.missingFields[0]").value("teacher"))
                .andExpect(jsonPath("$.missingFields[1]").value("course"))
                .andExpect(jsonPath("$.missingFields[2]").value("issuer"));

        assertThat(tempDir.resolve("certificates")).doesNotExist();
    }

    @Test
    void jsonMalformadoDebeResponderControladoSinCrearArchivos() throws Exception {
        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teacher\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("JSON inválido"))
                .andExpect(content().string(not(containsString("<html"))))
                .andExpect(content().string(not(containsString("trace"))));

        assertThat(tempDir.resolve("certificates")).doesNotExist();
    }

    @Test
    void errorPdfNoDebePersistirGeneracion() throws Exception {
        MockMvc failingPdfMockMvc = buildMockMvc(repository, new PdfGenerationService() {
            @Override
            public byte[] generateCourseCertificate(
                    pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest request,
                    CertificateGenerationMetadata metadata) {
                throw new PdfGenerationException("fallo interno del PDF");
            }

            @Override
            public byte[] generateSemesterCertificate(
                    pe.edu.unmsm.fisi.gestiondocente.constancia.dto.SemesterCertificateSourceSummary sourceSummary,
                    CertificateGenerationMetadata metadata) {
                throw new PdfGenerationException("fallo interno del PDF");
            }
        });

        failingPdfMockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("No se pudo generar el PDF de la constancia"))
                .andExpect(content().string(not(containsString("fallo interno"))));

        assertThat(repository.findByGenerationId("22200275-32BGNYGF-1-26.1-v001")).isEmpty();
        assertThat(tempDir.resolve("certificates")).doesNotExist();
    }

    @Test
    void errorStorageNoDebeDevolverExitoNiExponerRuta() throws Exception {
        CertificateGenerationRepository failingStorageRepository = mock(CertificateGenerationRepository.class);
        when(failingStorageRepository.existsApprovedByCertificateKey(any())).thenReturn(false);
        when(failingStorageRepository.nextVersion(any())).thenReturn(1);
        when(failingStorageRepository.saveGeneration(any(), any(), any()))
                .thenThrow(new StorageException("C:\\ruta\\sensible\\metadata.json"));
        MockMvc failingStorageMockMvc = buildMockMvc(failingStorageRepository, new PdfBoxPdfGenerationService());

        failingStorageMockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("No se pudo almacenar la constancia"))
                .andExpect(content().string(not(containsString("ruta"))))
                .andExpect(content().string(not(containsString("metadata.json"))));
    }

    @Test
    void identificadoresPeligrososDebenResponderBadRequestSinCrearArchivos() throws Exception {
        List<String> dangerousValues = List.of("../", "..", "abc/def", "abc\\def");
        List<String> jsonFields = List.of("teacher_code", "code", "section", "semester");

        for (String field : jsonFields) {
            for (String value : dangerousValues) {
                mockMvc.perform(post("/api/v1/constancias/curso")
                                .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonWith(field, value)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("La solicitud contiene campos invalidos"))
                        .andExpect(jsonPath("$.invalidFields[0].field").exists())
                        .andExpect(content().string(not(containsString(tempDir.toString()))));
            }
        }

        assertThat(tempDir.resolve("certificates")).doesNotExist();
    }

    @Test
    void constanciaAprobadaDebeBloquearNuevaGeneracion() throws Exception {
        CertificateGenerationMetadata approved = approvedMetadata();
        repository.saveGeneration(Map.of("request", "approved"), approved, "%PDF-approved".getBytes(StandardCharsets.US_ASCII));

        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "La constancia ya fue aprobada y no admite nuevas generaciones"));

        assertThat(repository.findHistoryByCertificateKey("22200275-32BGNYGF-1-26.1"))
                .hasSize(1)
                .allSatisfy(metadata -> assertThat(metadata.getStatus()).isEqualTo(EstadoConstancia.APROBADO));
        assertThat(generationDirectory(2)).doesNotExist();
    }

    @Test
    void casos404DebenSerControlados() throws Exception {
        mockMvc.perform(get("/api/v1/constancias/generaciones/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Generación de constancia no encontrada"));

        mockMvc.perform(get("/api/v1/constancias/certificados/no-existe/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        CertificateGenerationMetadata metadataWithoutPdf = generatedMetadata(1);
        createMetadataOnlyGeneration(metadataWithoutPdf);

        mockMvc.perform(get("/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/pdf"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("PDF de constancia no encontrado"));
    }

    private org.springframework.test.web.servlet.ResultActions postValidCertificate(String json) throws Exception {
        return mockMvc.perform(post("/api/v1/constancias/curso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private MockMvc buildMockMvc(CertificateGenerationRepository constanciaRepository,
            PdfGenerationService pdfGenerationService) {
        StoragePathSanitizer sanitizer = new StoragePathSanitizer();
        CourseCertificateService courseService = new CourseCertificateService(
                new CourseCertificateRequestValidator(),
                new CertificateIdService(sanitizer),
                constanciaRepository,
                pdfGenerationService,
                Clock.fixed(Instant.parse("2026-07-14T10:30:00Z"), ZoneId.of("UTC")));
        ConstanciaQueryService queryService = new ConstanciaQueryService(constanciaRepository);

        return MockMvcBuilders
                .standaloneSetup(new CourseCertificateController(courseService),
                        new ConstanciaQueryController(queryService))
                .setControllerAdvice(new ConstanciaExceptionHandler())
                .setMessageConverters(new ByteArrayHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private void assertStoredGeneration(Path generationDirectory, String generationId, int version) throws Exception {
        assertThat(generationDirectory.resolve("request.json")).exists();
        assertThat(generationDirectory.resolve("metadata.json")).exists();
        assertThat(generationDirectory.resolve("certificate.pdf")).exists();

        JsonNode request = objectMapper.readTree(generationDirectory.resolve("request.json").toFile());
        assertThat(request.at("/teacher/full_name").asText()).isEqualTo("José Muñoz Peña");
        assertThat(request.at("/course/subject").asText()).isEqualTo("Ingeniería y Gestión de Proyectos");

        CertificateGenerationMetadata metadata = objectMapper.readValue(
                generationDirectory.resolve("metadata.json").toFile(), CertificateGenerationMetadata.class);
        assertThat(metadata.getGenerationId()).isEqualTo(generationId);
        assertThat(metadata.getCertificateKey()).isEqualTo("22200275-32BGNYGF-1-26.1");
        assertThat(metadata.getVersion()).isEqualTo(version);
        assertThat(metadata.getType()).isEqualTo(TipoConstancia.CURSO);
        assertThat(metadata.getStatus()).isEqualTo(EstadoConstancia.GENERADO);
        assertThat(metadata.getTeacherCode()).isEqualTo("22200275");
        assertThat(metadata.getCourseCode()).isEqualTo("32BGNYGF");
        assertThat(metadata.getSection()).isEqualTo("1");
        assertThat(metadata.getSemester()).isEqualTo("26.1");
        assertThat(metadata.getGeneratedAt()).isEqualTo(Instant.parse("2026-07-14T10:30:00Z"));

        byte[] pdf = Files.readAllBytes(generationDirectory.resolve("certificate.pdf"));
        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    private Path generationDirectory(int version) {
        return tempDir.resolve("certificates")
                .resolve("course")
                .resolve("26.1")
                .resolve("22200275")
                .resolve("32BGNYGF-1")
                .resolve("v" + String.format("%03d", version));
    }

    private void createMetadataOnlyGeneration(CertificateGenerationMetadata metadata) throws Exception {
        Path generationDirectory = generationDirectory(metadata.getVersion());
        Files.createDirectories(generationDirectory);
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(generationDirectory.resolve("metadata.json").toFile(), metadata);
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(generationDirectory.resolve("request.json").toFile(), Map.of("request", "sin-pdf"));
    }

    private CertificateGenerationMetadata generatedMetadata(int version) {
        return new CertificateGenerationMetadata(
                "22200275-32BGNYGF-1-26.1-v" + String.format("%03d", version),
                "22200275-32BGNYGF-1-26.1",
                version,
                TipoConstancia.CURSO,
                EstadoConstancia.GENERADO,
                "22200275",
                "32BGNYGF",
                "1",
                "26.1",
                Instant.parse("2026-07-14T10:30:00Z"),
                "request.json",
                "certificate.pdf");
    }

    private CertificateGenerationMetadata approvedMetadata() {
        CertificateGenerationMetadata metadata = generatedMetadata(1);
        metadata.setStatus(EstadoConstancia.APROBADO);
        return metadata;
    }

    private String validJson() {
        return """
                {
                  "teacher": {
                    "full_name": "José Muñoz Peña",
                    "email": "jmunoz@unmsm.edu.pe",
                    "teacher_code": "22200275"
                  },
                  "course": {
                    "code": "32BGNYGF",
                    "subject": "Ingeniería y Gestión de Proyectos",
                    "cycle": "7",
                    "section": "1",
                    "school": "SW",
                    "plan": "2023",
                    "semester": "26.1"
                  },
                  "issuer": {
                    "system": "moodle",
                    "executed_by_userid": "12345",
                    "executed_by_email": "usuario@unmsm.edu.pe"
                  }
                }
                """;
    }

    private String validJsonForOtherTeacher() {
        return validJson()
                .replace("José Muñoz Peña", "Ana Torres Lima")
                .replace("jmunoz@unmsm.edu.pe", "atorres@unmsm.edu.pe")
                .replace("22200275", "22200999");
    }

    private String validJsonWith(String field, String value) {
        String escapedValue = value.replace("\\", "\\\\");
        return switch (field) {
            case "teacher_code" -> validJson().replace("\"teacher_code\": \"22200275\"",
                    "\"teacher_code\": \"" + escapedValue + "\"");
            case "code" -> validJson().replace("\"code\": \"32BGNYGF\"",
                    "\"code\": \"" + escapedValue + "\"");
            case "section" -> validJson().replace("\"section\": \"1\"",
                    "\"section\": \"" + escapedValue + "\"");
            case "semester" -> validJson().replace("\"semester\": \"26.1\"",
                    "\"semester\": \"" + escapedValue + "\"");
            default -> throw new IllegalArgumentException("Campo no soportado: " + field);
        };
    }
}
