package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CourseCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.ApprovedCertificateAlreadyExistsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingRequiredFieldsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.PdfGenerationException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.StorageException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.CourseCertificateService;

class CourseCertificateControllerTest {

    private CourseCertificateService service;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        service = mock(CourseCertificateService.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CourseCertificateController(service))
                .setControllerAdvice(new ConstanciaExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(
                        objectMapper))
                .build();
    }

    @Test
    void postCursoDebeDevolverCreatedYRespuesta() throws Exception {
        when(service.generateCourseCertificate(any(CourseCertificateRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generationId").value("22200275-32BGNYGF-1-26.1-v001"))
                .andExpect(jsonPath("$.certificateKey").value("22200275-32BGNYGF-1-26.1"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.type").value("CURSO"))
                .andExpect(jsonPath("$.status").value("GENERADO"))
                .andExpect(jsonPath("$.teacherFullName").value("Nombre completo docente"))
                .andExpect(jsonPath("$.courseCode").value("32BGNYGF"))
                .andExpect(jsonPath("$.courseSubject").value("Nombre del curso"))
                .andExpect(jsonPath("$.section").value("1"))
                .andExpect(jsonPath("$.semester").value("26.1"))
                .andExpect(jsonPath("$.generatedAt").value("2026-07-14T10:30:00"))
                .andExpect(jsonPath("$.viewUrl").value(
                        "/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/pdf"))
                .andExpect(jsonPath("$.downloadUrl").value(
                        "/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/download"));
    }

    @Test
    void teacherAusenteDebeDevolverBadRequestConMissingFields() throws Exception {
        when(service.generateCourseCertificate(any()))
                .thenThrow(new MissingRequiredFieldsException(List.of("teacher")));

        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"course\":{},\"issuer\":{}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MissingRequiredFieldsException.DEFAULT_MESSAGE))
                .andExpect(jsonPath("$.missingFields[0]").value("teacher"));
    }

    @Test
    void campoIndividualVacioDebeDevolverBadRequest() throws Exception {
        when(service.generateCourseCertificate(any()))
                .thenThrow(new MissingRequiredFieldsException(List.of("course.section")));

        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson().replace("\"section\":\"1\"", "\"section\":\"\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.missingFields[0]").value("course.section"));
    }

    @Test
    void variosCamposFaltantesDebenDevolverseJuntos() throws Exception {
        when(service.generateCourseCertificate(any()))
                .thenThrow(new MissingRequiredFieldsException(List.of("teacher.email", "course.section")));

        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.missingFields.length()").value(2))
                .andExpect(jsonPath("$.missingFields[0]").value("teacher.email"))
                .andExpect(jsonPath("$.missingFields[1]").value("course.section"));
    }

    @Test
    void jsonMalformadoDebeDevolverBadRequestControlado() throws Exception {
        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teacher\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("JSON inválido"))
                .andExpect(content().string(not(containsString("trace"))))
                .andExpect(content().string(not(containsString("<html"))));
    }

    @Test
    void constanciaAprobadaDebeDevolverConflict() throws Exception {
        when(service.generateCourseCertificate(any()))
                .thenThrow(new ApprovedCertificateAlreadyExistsException());

        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "La constancia ya fue aprobada y no admite nuevas generaciones"));
    }

    @Test
    void errorPdfDebeDevolverInternalServerErrorControlado() throws Exception {
        when(service.generateCourseCertificate(any()))
                .thenThrow(new PdfGenerationException("detalle interno"));

        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("No se pudo generar el PDF de la constancia"))
                .andExpect(content().string(not(containsString("detalle interno"))));
    }

    @Test
    void errorStorageDebeDevolverInternalServerErrorControlado() throws Exception {
        when(service.generateCourseCertificate(any()))
                .thenThrow(new StorageException("ruta sensible"));

        mockMvc.perform(post("/api/v1/constancias/curso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("No se pudo almacenar la constancia"))
                .andExpect(content().string(not(containsString("ruta sensible"))));
    }

    private CourseCertificateResponse response() {
        return new CourseCertificateResponse(
                "22200275-32BGNYGF-1-26.1-v001",
                "22200275-32BGNYGF-1-26.1",
                1,
                TipoConstancia.CURSO,
                EstadoConstancia.GENERADO,
                "Nombre completo docente",
                "32BGNYGF",
                "Nombre del curso",
                "1",
                "26.1",
                LocalDateTime.of(2026, 7, 14, 10, 30),
                "/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/pdf",
                "/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/download");
    }

    private String validJson() {
        return """
                {
                  "teacher": {
                    "full_name": "Nombre completo docente",
                    "email": "docente@unmsm.edu.pe",
                    "teacher_code": "22200275"
                  },
                  "course": {
                    "code": "32BGNYGF",
                    "subject": "Nombre del curso",
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
}
