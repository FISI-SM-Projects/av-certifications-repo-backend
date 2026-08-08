package pe.edu.unmsm.fisi.gestiondocente.usuario.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import pe.edu.unmsm.fisi.gestiondocente.usuario.entity.Usuario;

class UsuarioRepositoryImmutabilityTest {

    private final UsuarioRepository repository = new UsuarioRepository();

    @Test
    void modificarUsuarioRecibidoNoDebeAlterarRepositorio() {
        Usuario usuario = repository.findById(1L).orElseThrow();
        usuario.setTeacherCode("ALTERADO");
        usuario.setNombreCompleto("Nombre contaminado");

        Usuario posterior = repository.findById(1L).orElseThrow();

        assertThat(posterior.getTeacherCode()).isEqualTo("082026");
        assertThat(posterior.getNombreCompleto()).isNull();
    }
}
