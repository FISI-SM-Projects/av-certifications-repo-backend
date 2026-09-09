package pe.edu.unmsm.fisi.gestiondocente.docente.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Docente;

class DocenteRepositoryImmutabilityTest {

    private final DocenteRepository repository = new DocenteRepository();

    @Test
    void modificarDocenteRecibidoNoDebeAlterarRepositorio() {
        Docente docente = repository.findByCodigo("22200101").orElseThrow();
        docente.setCodigo("ALTERADO");
        docente.setCorreoInstitucional("otro@unmsm.edu.pe");

        Docente posterior = repository.findByCodigo("22200101").orElseThrow();

        assertThat(posterior.getCodigo()).isEqualTo("22200101");
        assertThat(posterior.getCorreoInstitucional()).isEqualTo("lalarconl@unmsm.edu.pe");
    }
}
