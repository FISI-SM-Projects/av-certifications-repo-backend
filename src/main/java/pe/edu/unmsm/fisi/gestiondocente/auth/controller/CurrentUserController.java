package pe.edu.unmsm.fisi.gestiondocente.auth.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.CurrentUserResponse;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.CurrentAccountService;

@RestController
@Profile("!test")
@RequestMapping("/api/v1/auth")
public class CurrentUserController {
    private final CurrentAccountService service;
    public CurrentUserController(CurrentAccountService service) { this.service = service; }
    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) { return service.me(authentication); }
}
