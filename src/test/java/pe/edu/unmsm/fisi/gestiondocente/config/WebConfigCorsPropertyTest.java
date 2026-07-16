package pe.edu.unmsm.fisi.gestiondocente.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.cors.allowed-origins=http://localhost:3100, http://localhost:3101")
@AutoConfigureMockMvc
class WebConfigCorsPropertyTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void corsDebeUsarOrigenesConfiguradosPorPropiedad() throws Exception {
        mockMvc.perform(options("/api/v1/constancias/curso")
                        .header("Origin", "http://localhost:3101")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin"))
                        .isEqualTo("http://localhost:3101"));
    }

    @Test
    void corsConfiguradoPorPropiedadDebeRechazarOrigenNoIncluido() throws Exception {
        mockMvc.perform(options("/api/v1/constancias/curso")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
