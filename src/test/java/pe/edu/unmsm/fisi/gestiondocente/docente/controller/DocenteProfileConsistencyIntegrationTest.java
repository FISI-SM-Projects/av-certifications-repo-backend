package pe.edu.unmsm.fisi.gestiondocente.docente.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import pe.edu.unmsm.fisi.gestiondocente.constancia.controller.ConstanciaExceptionHandler;
import pe.edu.unmsm.fisi.gestiondocente.constancia.controller.ConstanciaQueryController;
import pe.edu.unmsm.fisi.gestiondocente.constancia.controller.CourseCertificateController;
import pe.edu.unmsm.fisi.gestiondocente.constancia.controller.SemesterCertificateController;
import pe.edu.unmsm.fisi.gestiondocente.constancia.pdf.PdfBoxPdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.FileSystemConstanciaRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.CertificateIdService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.ConstanciaQueryService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.CourseCertificateService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.SemesterCertificateService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CourseCertificateRequestValidator;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.SemesterCertificateRequestValidator;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.StoragePathSanitizer;
import pe.edu.unmsm.fisi.gestiondocente.docente.mapper.DocenteMapper;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.DocenteRepository;
import pe.edu.unmsm.fisi.gestiondocente.docente.service.DocenteProfileQueryService;
import pe.edu.unmsm.fisi.gestiondocente.docente.service.DocenteService;

class DocenteProfileConsistencyIntegrationTest {

    @TempDir
    private Path tempDir;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        StoragePathSanitizer sanitizer = new StoragePathSanitizer();
        FileSystemConstanciaRepository repository = new FileSystemConstanciaRepository(tempDir, objectMapper,
                sanitizer);
        Clock fixedClock = Clock.fixed(Instant.parse("2026-07-14T10:30:00Z"), ZoneId.of("UTC"));
        PdfBoxPdfGenerationService pdfGenerationService = new PdfBoxPdfGenerationService();
        CertificateIdService certificateIdService = new CertificateIdService(sanitizer);
        CourseCertificateService courseCertificateService = new CourseCertificateService(
                new CourseCertificateRequestValidator(),
                certificateIdService,
                repository,
                pdfGenerationService,
                fixedClock);
        SemesterCertificateService semesterCertificateService = new SemesterCertificateService(
                new SemesterCertificateRequestValidator(),
                certificateIdService,
                repository,
                pdfGenerationService,
                fixedClock);
        ConstanciaQueryService constanciaQueryService = new ConstanciaQueryService(repository);
        DocenteRepository docenteRepository = new DocenteRepository();
        DocenteMapper docenteMapper = new DocenteMapper();
        DocenteService docenteService = new DocenteService(docenteRepository, docenteMapper);
        DocenteProfileQueryService profileQueryService = new DocenteProfileQueryService(docenteRepository,
                docenteMapper, constanciaQueryService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new CourseCertificateController(courseCertificateService),
                        new SemesterCertificateController(semesterCertificateService),
                        new ConstanciaQueryController(constanciaQueryService),
                        new DocenteController(docenteService, profileQueryService))
                .setControllerAdvice(new ConstanciaExceptionHandler(), new DocenteExceptionHandler())
                .setMessageConverters(new ByteArrayHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void perfilYListadoDebenMostrarLaMismaUltimaVersionPorCurso() throws Exception {
        postCourse(courseJson("082026", "32BGNYGF", "1", "Ingenieria y Gestion de Proyectos"))
                .andExpect(status().isCreated());
        postCourse(courseJson("082026", "32BGNYGF", "1", "Ingenieria y Gestion de Proyectos"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/constancias/docentes/082026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].generationId").value("082026-32BGNYGF-1-26.1-v002"))
                .andExpect(jsonPath("$[0].version").value(2));

        mockMvc.perform(get("/api/v1/docentes/082026/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docente.codigo").value("082026"))
                .andExpect(jsonPath("$.constancias.length()").value(1))
                .andExpect(jsonPath("$.constancias[0].generationId").value("082026-32BGNYGF-1-26.1-v002"))
                .andExpect(jsonPath("$.constancias[0].certificateKey").value("082026-32BGNYGF-1-26.1"))
                .andExpect(jsonPath("$.constancias[0].version").value(2))
                .andExpect(jsonPath("$.constancias[0].type").value("CURSO"))
                .andExpect(jsonPath("$.constancias[0].status").value("GENERADO"))
                .andExpect(content().string(not(containsString("082026-32BGNYGF-1-26.1-v001"))))
                .andExpect(content().string(not(containsString("demo-2026-I.pdf"))));
    }

    @Test
    void perfilDebeMostrarConstanciaSemestralReal() throws Exception {
        postCourse(courseJson("082026", "32BGNYGF", "1", "Arquitectura de Software"))
                .andExpect(status().isCreated());
        postCourse(courseJson("082026", "32SW001", "2", "Ingenieria de Requisitos"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/constancias/semestral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teacher_code": "082026",
                                  "semester": "26.1",
                                  "expected_courses": [
                                    { "code": "32BGNYGF", "section": "1" },
                                    { "code": "32SW001", "section": "2" }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generationId").value("082026-26.1-v001"))
                .andExpect(jsonPath("$.type").value("SEMESTRAL"));

        mockMvc.perform(get("/api/v1/docentes/082026/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.constancias.length()").value(3))
                .andExpect(jsonPath("$.constancias[?(@.type == 'SEMESTRAL')].generationId")
                        .value("082026-26.1-v001"))
                .andExpect(content().string(containsString("\"courseCode\":null")))
                .andExpect(content().string(containsString("\"section\":null")));
    }

    @Test
    void perfilesDebenSepararDocentesYPermitirListaVacia() throws Exception {
        postCourse(courseJson("082026", "32BGNYGF", "1", "Arquitectura de Software"))
                .andExpect(status().isCreated());
        postCourse(courseJson("082028", "32CC001", "1", "Algoritmos Avanzados"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/docentes/082026/perfil"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("082026-32BGNYGF-1-26.1-v001")))
                .andExpect(content().string(not(containsString("082028-32CC001-1-26.1-v001"))));

        mockMvc.perform(get("/api/v1/docentes/082028/perfil"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("082028-32CC001-1-26.1-v001")))
                .andExpect(content().string(not(containsString("082026-32BGNYGF-1-26.1-v001"))));

        mockMvc.perform(get("/api/v1/docentes/082027/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docente.codigo").value("082027"))
                .andExpect(jsonPath("$.constancias.length()").value(0))
                .andExpect(content().string(not(containsString("demo-2026-I.pdf"))));
    }

    private org.springframework.test.web.servlet.ResultActions postCourse(String json) throws Exception {
        return mockMvc.perform(post("/api/v1/constancias/curso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private String courseJson(String teacherCode, String courseCode, String section, String subject) {
        String fullName = switch (teacherCode) {
            case "082026" -> "Juan Carlos P\u00e9rez G\u00f3mez";
            case "082027" -> "Mar\u00eda Elena Torres Rojas";
            case "082028" -> "Carlos Alberto Ramos Silva";
            default -> "Juan Carlos P\u00e9rez G\u00f3mez";
        };
        String email = switch (teacherCode) {
            case "082026" -> "jperez@unmsm.edu.pe";
            case "082027" -> "mtorres@unmsm.edu.pe";
            case "082028" -> "cramos@unmsm.edu.pe";
            default -> "jperez@unmsm.edu.pe";
        };

        return """
                {
                  "teacher": {
                    "full_name": "%s",
                    "email": "%s",
                    "teacher_code": "%s"
                  },
                  "course": {
                    "code": "%s",
                    "subject": "%s",
                    "cycle": "7",
                    "section": "%s",
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
                """.formatted(fullName, email, teacherCode, courseCode, subject, section);
    }
}
