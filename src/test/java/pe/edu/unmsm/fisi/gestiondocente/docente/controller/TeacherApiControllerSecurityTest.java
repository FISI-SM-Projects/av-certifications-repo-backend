package pe.edu.unmsm.fisi.gestiondocente.docente.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.JwtService;
import pe.edu.unmsm.fisi.gestiondocente.auth.util.JwtAuthenticationEntryPoint;
import pe.edu.unmsm.fisi.gestiondocente.auth.util.JwtFilter;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.api.TeacherCourseResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.api.TeacherMeResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.service.TeacherApiService;
import pe.edu.unmsm.fisi.gestiondocente.shared.config.SecurityConfig;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.GlobalExceptionHandler;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.PaginatedResponse;

@SpringJUnitConfig(TeacherApiControllerSecurityTest.Config.class)
@WebAppConfiguration
@ActiveProfiles("institutional-security-test")
@TestPropertySource(properties = {
        "ldap.url=ldap://localhost:53389", "ldap.base.dn=dc=unmsm,dc=edu,dc=pe",
        "ldap.manager.dn=", "ldap.manager.password=", "ldap.user.search.base=", "ldap.user.search.filter=(uid={0})",
        "jwt.secret=7l9TsWcso/AIyGSz5K1z1YtkPzYz+4c8awtPCOWLr+k=", "jwt.expiration=3600000"
})
class TeacherApiControllerSecurityTest {
    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, JwtFilter.class, JwtService.class, JwtAuthenticationEntryPoint.class,
            TeacherApiController.class, GlobalExceptionHandler.class})
    static class Config {
        @Bean TeacherApiService teacherApiService() { return mock(TeacherApiService.class); }
    }

    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy filter;
    @Autowired JwtService jwt;
    @Autowired TeacherApiService service;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(filter).build();
        when(service.me(any())).thenReturn(new TeacherMeResponse(2L, 10L, 46L, "22200101",
                "22233344", "lalarconl@unmsm.edu.pe", "LUIS ALBERTO", "ALARCON", "LOAYZA", "CC", "ACTIVO"));
        when(service.courses(any(), any(), any(), any(), any(), any(), any())).thenReturn(new TeacherApiService.CoursePage(
                List.of(new TeacherCourseResponse(1L, 101L, 2L, 8, 1, 2018, "SW",
                        new TeacherCourseResponse.CourseSummary(1L, "202W0701", "Ingeniería de Software I"),
                        new TeacherCourseResponse.AcademicPeriodSummary(1L, "26.1",
                                OffsetDateTime.parse("2026-03-15T00:00:00Z"),
                                OffsetDateTime.parse("2026-07-20T00:00:00Z")))),
                new PaginatedResponse.Pagination(0, 10, 1, 1, 1)));
        when(service.teachers(any(), any(), any(), any())).thenReturn(new TeacherApiService.TeacherPage(
                List.of(new TeacherMeResponse(2L, 10L, 46L, "22200101",
                        "22233344", "lalarconl@unmsm.edu.pe", "LUIS ALBERTO", "ALARCON", "LOAYZA", "CC", "ACTIVO")),
                new PaginatedResponse.Pagination(0, 10, 1, 1, 1)));
    }

    @Test
    void teacherMeRequiresToken() throws Exception {
        mvc.perform(get("/api/v1/teachers/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(401));
    }

    @Test
    void validJwtCanReadTeacherMeWithEnvelope() throws Exception {
        mvc.perform(get("/api/v1/teachers/me").header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Operación completada exitosamente"))
                .andExpect(jsonPath("$.data.code").value("22200101"))
                .andExpect(jsonPath("$.data.email").value("lalarconl@unmsm.edu.pe"));
    }

    @Test
    void coursesReturnEnvelopeAndPagination() throws Exception {
        mvc.perform(get("/api/v1/teachers/me/courses")
                        .param("semester", "26.1")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].course.code").value("202W0701"))
                .andExpect(jsonPath("$.pagination.pageNumber").value(0))
                .andExpect(jsonPath("$.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    void teachersCollectionRequiresToken() throws Exception {
        mvc.perform(get("/api/v1/teachers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(401));
    }

    @Test
    void teachersCollectionReturnsEnvelopeAndPagination() throws Exception {
        mvc.perform(get("/api/v1/teachers")
                        .param("department", "CC")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token("DIRECTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("22200101"))
                .andExpect(jsonPath("$.pagination.pageNumber").value(0))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    private String token() {
        return token("DOCENTE");
    }

    private String token(String role) {
        return jwt.generateToken(Map.of("roles", List.of("ROLE_" + role), "accountId", 1), "lalarconl");
    }
}
