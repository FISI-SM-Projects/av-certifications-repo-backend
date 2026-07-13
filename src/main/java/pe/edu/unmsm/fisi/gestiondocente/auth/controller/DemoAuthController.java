package pe.edu.unmsm.fisi.gestiondocente.auth.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.unmsm.fisi.gestiondocente.auth.dto.DemoLoginRequest;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.DemoLoginResponse;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.DemoAuthService;
import pe.edu.unmsm.fisi.gestiondocente.usuario.dto.UsuarioSesionDto;

@RestController
@RequestMapping("/api/v1/auth")
public class DemoAuthController {

    private final DemoAuthService demoAuthService;

    public DemoAuthController(DemoAuthService demoAuthService) {
        this.demoAuthService = demoAuthService;
    }

    @GetMapping("/demo-users")
    public List<UsuarioSesionDto> listarUsuariosDemo() {
        return demoAuthService.listarUsuariosDemo();
    }

    @PostMapping("/demo-login")
    public DemoLoginResponse login(@RequestBody(required = false) DemoLoginRequest request) {
        return demoAuthService.login(request);
    }
}
