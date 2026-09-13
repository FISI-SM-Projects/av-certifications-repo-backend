package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.JwtService;
import pe.edu.unmsm.fisi.gestiondocente.auth.util.JwtAuthenticationEntryPoint;
import pe.edu.unmsm.fisi.gestiondocente.auth.util.JwtFilter;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.api.CertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.InstitutionalCertificateService;
import pe.edu.unmsm.fisi.gestiondocente.shared.config.SecurityConfig;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.GlobalExceptionHandler;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.PaginatedResponse;

@SpringJUnitConfig(CertificateApiControllerSecurityTest.Config.class)
@WebAppConfiguration
@ActiveProfiles("institutional-security-test")
@TestPropertySource(properties = {
        "ldap.url=ldap://localhost:53389", "ldap.base.dn=dc=unmsm,dc=edu,dc=pe",
        "ldap.manager.dn=", "ldap.manager.password=", "ldap.user.search.base=", "ldap.user.search.filter=(uid={0})",
        "jwt.secret=7l9TsWcso/AIyGSz5K1z1YtkPzYz+4c8awtPCOWLr+k=", "jwt.expiration=3600000"
})
class CertificateApiControllerSecurityTest {
    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, JwtFilter.class, JwtService.class, JwtAuthenticationEntryPoint.class,
            CertificateApiController.class, GlobalExceptionHandler.class})
    static class Config {
        @Bean InstitutionalCertificateService institutionalCertificateService() {
            return mock(InstitutionalCertificateService.class);
        }
    }

    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy filter;
    @Autowired JwtService jwt;
    @Autowired InstitutionalCertificateService service;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(filter).build();
        var certificate = certificate();
        when(service.listApi(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                new InstitutionalCertificateService.CertificatePage(
                        List.of(certificate),
                        new PaginatedResponse.Pagination(0, 10, 1, 1, 1)));
        when(service.createApi(any(), any())).thenReturn(certificate);
        when(service.detailApi(eq("1"), any())).thenReturn(certificate);
        when(service.versionsApi(eq("1"), any(), any(), any())).thenReturn(
                new InstitutionalCertificateService.CertificatePage(
                        List.of(certificate),
                        new PaginatedResponse.Pagination(0, 10, 1, 1, 1)));
        when(service.signApi(eq("1"), any())).thenReturn(certificate);
        when(service.readPdf(eq("1"), any())).thenReturn("%PDF-test".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void certificatesRequireToken() throws Exception {
        mvc.perform(get("/api/v1/certificates"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(401));
    }

    @Test
    void listReturnsEnvelopeAndPagination() throws Exception {
        mvc.perform(get("/api/v1/certificates")
                        .param("certificateType", "COURSE")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].certificateType").value("COURSE"))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    void createCourseReturnsEnvelope() throws Exception {
        mvc.perform(post("/api/v1/certificates")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"certificateType\":\"COURSE\",\"academicWorkloadId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void incompleteSemesterConflictUsesStandardError() throws Exception {
        when(service.createApi(any(), any())).thenThrow(new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Aun no se han generado constancias para todos los cursos del periodo"));

        mvc.perform(post("/api/v1/certificates")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"certificateType\":\"SEMESTER\",\"teacherCode\":\"22200101\",\"semester\":\"26.1\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    void detailAndDocumentUseOfficialPaths() throws Exception {
        mvc.perform(get("/api/v1/certificates/1").header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentUrl").value("/api/v1/certificates/1/document"));

        mvc.perform(get("/api/v1/certificates/1/document")
                        .param("disposition", "inline")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void versionsAndSignatureReturnOfficialEnvelope() throws Exception {
        mvc.perform(get("/api/v1/certificates/1/versions").header("Authorization", "Bearer " + token("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.totalElements").value(1));

        mvc.perform(post("/api/v1/certificates/1/signature").header("Authorization", "Bearer " + token("DIRECTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("GENERADA"));
    }

    private CertificateResponse certificate() {
        return new CertificateResponse(1L, "workload-1", "COURSE", "GENERADA", 1,
                2L, "22200101", "LUIS ALBERTO ALARCON LOAYZA", 3L, "26.1", 4L,
                new CertificateResponse.CourseSummary(5L, "202W0701", "Ingeniería de Software I"),
                1, 8, "SW", 2018, Instant.parse("2026-09-12T00:00:00Z"), null,
                true, "/api/v1/certificates/1/document");
    }

    private String token() {
        return token("DOCENTE");
    }

    private String token(String role) {
        return jwt.generateToken(Map.of("roles", List.of("ROLE_" + role), "accountId", 1), "lalarconl");
    }
}
