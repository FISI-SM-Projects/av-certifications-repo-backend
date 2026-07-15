package pe.edu.unmsm.fisi.gestiondocente.constancia.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EstadoConstanciaTest {

    @Test
    void estadosDebenMantenerValoresDelSprintUno() {
        assertThat(EstadoConstancia.values())
                .containsExactly(EstadoConstancia.GENERADO, EstadoConstancia.APROBADO);
    }

    @Test
    void tiposDebenMantenerValoresDelSprintTres() {
        assertThat(TipoConstancia.values())
                .containsExactly(TipoConstancia.CURSO, TipoConstancia.SEMESTRAL);
    }
}
