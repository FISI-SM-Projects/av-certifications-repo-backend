package pe.edu.unmsm.fisi.gestiondocente.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.LoginRequest;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.LoginResponse;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.LoginService;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.DefaultResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<DefaultResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ResponseEntity.ok(DefaultResponse.success("Login exitoso", response));
    }
}
