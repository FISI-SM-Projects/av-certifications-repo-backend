package pe.edu.unmsm.fisi.gestiondocente.docente.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DocenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void perfilDocenteDemoDebeMantenerContratoSprintUno() throws Exception {
        mockMvc.perform(get("/api/v1/docentes/demo/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docente.codigo").value("082026"))
                .andExpect(jsonPath("$.docente.nombres").value("Juan Carlos"))
                .andExpect(jsonPath("$.docente.apellidos").value("Pérez Gómez"))
                .andExpect(jsonPath("$.docente.departamentoAcademico").value("Ingeniería de Software"))
                .andExpect(jsonPath("$.docente.escuelaProfesional").doesNotExist())
                .andExpect(jsonPath("$.constancias").isArray())
                .andExpect(jsonPath("$.constancias.length()").value(2))
                .andExpect(jsonPath("$.constancias[*].estado", containsInAnyOrder("GENERADO", "APROBADO")));
    }

    @Test
    void listarDocentesDeIngenieriaDeSoftwareDebeAislarDepartamento() throws Exception {
        mockMvc.perform(get("/api/v1/director/docentes")
                        .param("departamentoAcademico", "Ingeniería de Software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[*].departamentoAcademico", everyItem(is("Ingeniería de Software"))))
                .andExpect(content().string(not(containsString("Ciencia de la Computación"))))
                .andExpect(content().string(not(containsString("codigo"))));
    }

    @Test
    void listarDocentesDeCienciaDeLaComputacionDebeAislarDepartamento() throws Exception {
        mockMvc.perform(get("/api/v1/director/docentes")
                        .param("departamentoAcademico", "Ciencia de la Computación"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[*].departamentoAcademico", everyItem(is("Ciencia de la Computación"))))
                .andExpect(content().string(not(containsString("Ingeniería de Software"))))
                .andExpect(content().string(not(containsString("codigo"))));
    }

    @Test
    void listarDocentesDeDepartamentoInexistenteDebeDevolverListaVacia() throws Exception {
        mockMvc.perform(get("/api/v1/director/docentes")
                        .param("departamentoAcademico", "Departamento Inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listarDocentesSinDepartamentoDebeDevolverBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/director/docentes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El departamento académico es obligatorio"));
    }

    @Test
    void listarDocentesConDepartamentoVacioDebeDevolverBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/director/docentes")
                        .param("departamentoAcademico", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El departamento académico es obligatorio"));
    }

    @Test
    void perfilPorCodigoValidoDebeDevolverJuanCarlos() throws Exception {
        mockMvc.perform(get("/api/v1/docentes/082026/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docente.codigo").value("082026"))
                .andExpect(jsonPath("$.docente.nombres").value("Juan Carlos"))
                .andExpect(jsonPath("$.constancias").isArray())
                .andExpect(jsonPath("$.constancias.length()").value(2))
                .andExpect(jsonPath("$.docente.apellidos").value("Pérez Gómez"))
                .andExpect(jsonPath("$.docente.departamentoAcademico").value("Ingeniería de Software"));
    }

    @Test
    void perfilPorCodigoValidoDeOtroDepartamentoDebeDevolverCarlosRamos() throws Exception {
        mockMvc.perform(get("/api/v1/docentes/082028/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docente.codigo").value("082028"))
                .andExpect(jsonPath("$.constancias").isArray())
                .andExpect(jsonPath("$.docente.nombres").value("Carlos Alberto"))
                .andExpect(jsonPath("$.docente.apellidos").value("Ramos Silva"))
                .andExpect(jsonPath("$.docente.departamentoAcademico").value("Ciencia de la Computación"));
    }

    @Test
    void perfilPorCodigoInexistenteDebeDevolverNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/docentes/999999/perfil"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Docente no encontrado"))
                .andExpect(content().string(not(containsString("trace"))))
                .andExpect(content().string(not(containsString("<html"))));
    }
}
