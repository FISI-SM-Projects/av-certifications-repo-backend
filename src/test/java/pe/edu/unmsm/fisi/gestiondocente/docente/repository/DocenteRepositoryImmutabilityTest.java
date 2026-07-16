package pe.edu.unmsm.fisi.gestiondocente.docente.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Docente;

class DocenteRepositoryImmutabilityTest {

    private final DocenteRepository repository = new DocenteRepository();

    @Test
    void modificarDocenteRecibidoNoDebeAlterarRepositorio() {
        Docente docente = repository.findByCodigo("082026").orElseThrow();
        docente.setCodigo("ALTERADO");
        docente.setCorreoInstitucional("otro@unmsm.edu.pe");

        Docente posterior = repository.findByCodigo("082026").orElseThrow();

        assertThat(posterior.getCodigo()).isEqualTo("082026");
        assertThat(posterior.getCorreoInstitucional()).isEqualTo("jperez@unmsm.edu.pe");
    }
}
