package pe.edu.unmsm.fisi.gestiondocente.entity.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RolUsuarioTest {

    @Test
    void rolesDebenMantenerValoresDelSprintDos() {
        assertThat(RolUsuario.values())
                .containsExactly(RolUsuario.DOCENTE, RolUsuario.DIRECTOR, RolUsuario.ADMIN);
    }
}
