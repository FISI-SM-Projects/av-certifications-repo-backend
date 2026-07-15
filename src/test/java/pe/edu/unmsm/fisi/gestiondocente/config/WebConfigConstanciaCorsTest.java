package pe.edu.unmsm.fisi.gestiondocente.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebConfigConstanciaCorsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void corsDebePermitirPostDeConstanciasDesdeOrigenesLocales() throws Exception {
        mockMvc.perform(options("/api/v1/constancias/curso")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin"))
                            .isEqualTo("http://localhost:3000");
                    assertThat(result.getResponse().getHeader("Access-Control-Allow-Methods"))
                            .contains("GET")
                            .contains("POST")
                            .doesNotContain("PUT")
                            .doesNotContain("PATCH")
                            .doesNotContain("DELETE");
                    assertThat(result.getResponse().getHeader("Access-Control-Allow-Credentials")).isNull();
                });
    }

    @Test
    void corsDebePermitirGetDeConsultasDesdePuertoAlternativo() throws Exception {
        mockMvc.perform(options("/api/v1/constancias/docentes/22200275")
                        .header("Origin", "http://localhost:3001")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin"))
                        .isEqualTo("http://localhost:3001"));
    }

    @Test
    void corsDebeRechazarOrigenNoConfigurado() throws Exception {
        mockMvc.perform(options("/api/v1/constancias/curso")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
