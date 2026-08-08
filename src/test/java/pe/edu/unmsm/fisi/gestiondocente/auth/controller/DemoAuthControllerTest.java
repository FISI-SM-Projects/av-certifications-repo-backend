package pe.edu.unmsm.fisi.gestiondocente.auth.controller;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DemoAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarUsuariosDemoDebeDevolverOk() throws Exception {
        mockMvc.perform(get("/api/v1/auth/demo-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[?(@.role == 'DOCENTE')]", hasSize(3)))
                .andExpect(jsonPath("$[?(@.role == 'DIRECTOR')]", hasSize(2)))
                .andExpect(jsonPath("$[?(@.role == 'ADMIN')]", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Juan Carlos Pérez Gómez"))
                .andExpect(jsonPath("$[0].email").value("jperez@unmsm.edu.pe"))
                .andExpect(jsonPath("$[0].role").value("DOCENTE"));
    }

    @Test
    void loginDocenteDebeDevolverOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jperez@unmsm.edu.pe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("DOCENTE"))
                .andExpect(jsonPath("$.user.teacherCode").value("082026"));
    }

    @Test
    void loginDirectorDebeDevolverOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"director.software@unmsm.edu.pe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("DIRECTOR"))
                .andExpect(jsonPath("$.user.departamentoAcademico").value("Ingeniería de Software"))
                .andExpect(jsonPath("$.user.teacherCode").isEmpty());
    }

    @Test
    void loginAdminDebeDevolverOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@unmsm.edu.pe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.user.departamentoAcademico").isEmpty())
                .andExpect(jsonPath("$.user.teacherCode").isEmpty());
    }

    @Test
    void loginUsuarioInexistenteDebeDevolverNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"noexiste@unmsm.edu.pe\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario demo no encontrado"));
    }

    @Test
    void loginEmailVacioDebeDevolverBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El correo es obligatorio"));
    }

    @Test
    void loginEmailAusenteDebeDevolverBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El correo es obligatorio"));
    }

    @Test
    void loginSinBodyDebeDevolverBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El correo es obligatorio"));
    }

    @Test
    void loginJsonMalformadoDebeDevolverBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("trace"))))
                .andExpect(content().string(not(containsString("<html"))));
    }

    @Test
    void loginEmailConEspaciosDebeDevolverOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"  director.software@unmsm.edu.pe  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("DIRECTOR"))
                .andExpect(jsonPath("$.user.email").value("director.software@unmsm.edu.pe"));
    }

    @Test
    void loginEmailConMayusculasDebeDevolverOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"DIRECTOR.SOFTWARE@UNMSM.EDU.PE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("DIRECTOR"))
                .andExpect(jsonPath("$.user.email").value("director.software@unmsm.edu.pe"));
    }

    @Test
    void respuestasNoDebenExponerCodigo() throws Exception {
        mockMvc.perform(get("/api/v1/auth/demo-users"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("codigo"))))
                .andExpect(content().string(not(containsString("token"))))
                .andExpect(content().string(not(containsString("password"))));

        mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jperez@unmsm.edu.pe\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("codigo"))))
                .andExpect(content().string(not(containsString("token"))))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void healthDebeSeguirFuncionando() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void perfilDocenteDemoDebeSeguirFuncionando() throws Exception {
        mockMvc.perform(get("/api/v1/docentes/demo/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docente.departamentoAcademico").exists())
                .andExpect(jsonPath("$.docente.escuelaProfesional").doesNotExist())
                .andExpect(jsonPath("$.constancias").isArray());
    }

    @Test
    void corsDebePermitirSoloOrigenesYMetodosDeDesarrolloNecesarios() throws Exception {
        mockMvc.perform(options("/api/v1/auth/demo-login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andExpect(result -> {
                    String allowedOrigin = result.getResponse().getHeader("Access-Control-Allow-Origin");
                    String allowedMethods = result.getResponse().getHeader("Access-Control-Allow-Methods");
                    String allowCredentials = result.getResponse().getHeader("Access-Control-Allow-Credentials");

                    org.assertj.core.api.Assertions.assertThat(allowedOrigin)
                            .isEqualTo("http://localhost:3000");
                    org.assertj.core.api.Assertions.assertThat(allowedMethods)
                            .contains("GET")
                            .contains("POST")
                            .doesNotContain("PUT")
                            .doesNotContain("PATCH")
                            .doesNotContain("DELETE");
                    org.assertj.core.api.Assertions.assertThat(allowCredentials).isNull();
                });
    }

    @Test
    void corsNoDebePermitirOrigenNoConfigurado() throws Exception {
        mockMvc.perform(options("/api/v1/auth/demo-login")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
