package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CertificateGenerationResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.CertificateGenerationNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.CertificatePdfNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.ConstanciaQueryService;

class ConstanciaQueryControllerTest {

    private ConstanciaQueryService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(ConstanciaQueryService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ConstanciaQueryController(service))
                .setControllerAdvice(new ConstanciaExceptionHandler())
                .setMessageConverters(new ByteArrayHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void listarPorDocenteDebeDevolverOk() throws Exception {
        when(service.listLatestByTeacherCode("22200275")).thenReturn(List.of(response(1), response(2)));

        mockMvc.perform(get("/api/v1/constancias/docentes/22200275"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].generationId").value("22200275-32BGNYGF-1-26.1-v001"))
                .andExpect(jsonPath("$[0].teacherCode").value("22200275"))
                .andExpect(jsonPath("$[0].type").value("CURSO"))
                .andExpect(jsonPath("$[0].status").value("GENERADO"))
                .andExpect(content().string(not(containsString("requestFile"))))
                .andExpect(content().string(not(containsString("pdfFile"))))
                .andExpect(content().string(not(containsString("storage"))));
    }

    @Test
    void consultarGeneracionDebeDevolverOk() throws Exception {
        when(service.findByGenerationId("22200275-32BGNYGF-1-26.1-v001")).thenReturn(response(1));

        mockMvc.perform(get("/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generationId").value("22200275-32BGNYGF-1-26.1-v001"))
                .andExpect(jsonPath("$.certificateKey").value("22200275-32BGNYGF-1-26.1"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.viewUrl").value(
                        "/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/pdf"));
    }

    @Test
    void historialDebeDevolverListaOrdenada() throws Exception {
        when(service.findHistoryByCertificateKey("22200275-32BGNYGF-1-26.1"))
                .thenReturn(List.of(response(1), response(2)));

        mockMvc.perform(get("/api/v1/constancias/certificados/22200275-32BGNYGF-1-26.1/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].version").value(1))
                .andExpect(jsonPath("$[1].version").value(2));
    }

    @Test
    void visualizarPdfDebeResponderInlineApplicationPdf() throws Exception {
        when(service.readPdf("22200275-32BGNYGF-1-26.1-v001")).thenReturn("%PDF-test".getBytes());

        mockMvc.perform(get("/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("inline")))
                .andExpect(content().bytes("%PDF-test".getBytes()));
    }

    @Test
    void descargarPdfDebeResponderAttachmentApplicationPdf() throws Exception {
        when(service.readPdf("22200275-32BGNYGF-1-26.1-v001")).thenReturn("%PDF-test".getBytes());

        mockMvc.perform(get("/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("22200275-32BGNYGF-1-26.1-v001.pdf")));
    }

    @Test
    void generacionInexistenteDebeDevolverNotFoundJson() throws Exception {
        when(service.findByGenerationId("inexistente"))
                .thenThrow(new CertificateGenerationNotFoundException("inexistente"));

        mockMvc.perform(get("/api/v1/constancias/generaciones/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Generación de constancia no encontrada"))
                .andExpect(content().string(not(containsString("<html"))))
                .andExpect(content().string(not(containsString("trace"))));
    }

    @Test
    void pdfInexistenteDebeDevolverNotFoundJson() throws Exception {
        when(service.readPdf("22200275-32BGNYGF-1-26.1-v001"))
                .thenThrow(new CertificatePdfNotFoundException("22200275-32BGNYGF-1-26.1-v001"));

        mockMvc.perform(get("/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/pdf"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("PDF de constancia no encontrado"));
    }

    private CertificateGenerationResponse response(int version) {
        String generationId = "22200275-32BGNYGF-1-26.1-v" + String.format("%03d", version);
        return new CertificateGenerationResponse(
                generationId,
                "22200275-32BGNYGF-1-26.1",
                version,
                TipoConstancia.CURSO,
                EstadoConstancia.GENERADO,
                "22200275",
                "32BGNYGF",
                "1",
                "26.1",
                Instant.parse("2026-07-14T10:30:00Z"),
                "/api/v1/constancias/generaciones/" + generationId + "/pdf",
                "/api/v1/constancias/generaciones/" + generationId + "/download");
    }
}
