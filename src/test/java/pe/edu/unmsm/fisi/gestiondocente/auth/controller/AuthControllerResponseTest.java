package pe.edu.unmsm.fisi.gestiondocente.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.LoginRequest;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.LoginResponse;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.LoginService;

class AuthControllerResponseTest {
    @Test
    void loginUsesApprovedSuccessMessageAndEnvelope() {
        LoginService service = mock(LoginService.class);
        when(service.login(any())).thenReturn(new LoginResponse("token", "Bearer"));

        var response = new AuthController(service).login(new LoginRequest("lalarconl", "password"));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Operación completada exitosamente");
        assertThat(response.getBody().data().token()).isEqualTo("token");
    }
}
