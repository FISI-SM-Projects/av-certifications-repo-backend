package pe.edu.unmsm.fisi.gestiondocente.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pe.edu.unmsm.fisi.gestiondocente.auth.dto.DemoLoginRequest;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.DemoLoginResponse;
import pe.edu.unmsm.fisi.gestiondocente.auth.exception.DemoUserNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.usuario.dto.UsuarioSesionDto;
import pe.edu.unmsm.fisi.gestiondocente.usuario.entity.RolUsuario;
import pe.edu.unmsm.fisi.gestiondocente.usuario.mapper.UsuarioMapper;
import pe.edu.unmsm.fisi.gestiondocente.usuario.repository.UsuarioRepository;

class DemoAuthServiceTest {

    private DemoAuthService demoAuthService;

    @BeforeEach
    void setUp() {
        demoAuthService = new DemoAuthService(new UsuarioRepository(), new UsuarioMapper());
    }

    @Test
    void listarUsuariosDemoDebeRetornarUsuariosConfigurados() {
        List<UsuarioSesionDto> usuarios = demoAuthService.listarUsuariosDemo();

        assertThat(usuarios).hasSize(6);
        assertThat(usuarios).filteredOn(usuario -> usuario.getRole() == RolUsuario.DOCENTE).hasSize(3);
        assertThat(usuarios).filteredOn(usuario -> usuario.getRole() == RolUsuario.DIRECTOR).hasSize(2);
        assertThat(usuarios).filteredOn(usuario -> usuario.getRole() == RolUsuario.ADMIN).hasSize(1);
    }

    @Test
    void loginDebeRetornarUsuarioDocente() {
        DemoLoginResponse response = demoAuthService.login(new DemoLoginRequest("jperez@unmsm.edu.pe"));

        assertThat(response.getUser().getRole()).isEqualTo(RolUsuario.DOCENTE);
        assertThat(response.getUser().getTeacherCode()).isEqualTo("082026");
    }

    @Test
    void loginDebeRetornarUsuarioDirector() {
        DemoLoginResponse response = demoAuthService.login(new DemoLoginRequest("director.software@unmsm.edu.pe"));

        assertThat(response.getUser().getRole()).isEqualTo(RolUsuario.DIRECTOR);
        assertThat(response.getUser().getDepartamentoAcademico()).isEqualTo("Ingeniería de Software");
        assertThat(response.getUser().getTeacherCode()).isNull();
    }

    @Test
    void loginDebeRetornarUsuarioAdmin() {
        DemoLoginResponse response = demoAuthService.login(new DemoLoginRequest("admin@unmsm.edu.pe"));

        assertThat(response.getUser().getRole()).isEqualTo(RolUsuario.ADMIN);
        assertThat(response.getUser().getDepartamentoAcademico()).isNull();
        assertThat(response.getUser().getTeacherCode()).isNull();
    }

    @Test
    void loginDebeRechazarEmailInexistente() {
        assertThatThrownBy(() -> demoAuthService.login(new DemoLoginRequest("noexiste@unmsm.edu.pe")))
                .isInstanceOf(DemoUserNotFoundException.class)
                .hasMessage("Usuario demo no encontrado");
    }

    @Test
    void loginDebeRechazarEmailVacio() {
        assertThatThrownBy(() -> demoAuthService.login(new DemoLoginRequest("")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El correo es obligatorio");
    }

    @Test
    void loginDebeRechazarEmailNulo() {
        assertThatThrownBy(() -> demoAuthService.login(new DemoLoginRequest(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El correo es obligatorio");
    }

    @Test
    void loginDebeAceptarEmailConEspaciosLaterales() {
        DemoLoginResponse response = demoAuthService.login(new DemoLoginRequest("  mtorres@unmsm.edu.pe  "));

        assertThat(response.getUser().getEmail()).isEqualTo("mtorres@unmsm.edu.pe");
        assertThat(response.getUser().getRole()).isEqualTo(RolUsuario.DOCENTE);
    }

    @Test
    void loginDebeAceptarEmailConMayusculasDistintas() {
        DemoLoginResponse response = demoAuthService.login(new DemoLoginRequest("DIRECTOR.COMPUTACION@UNMSM.EDU.PE"));

        assertThat(response.getUser().getEmail()).isEqualTo("director.computacion@unmsm.edu.pe");
        assertThat(response.getUser().getRole()).isEqualTo(RolUsuario.DIRECTOR);
    }
}
