package pe.edu.unmsm.fisi.gestiondocente.auth.controller;

import pe.edu.unmsm.fisi.gestiondocente.auth.util.JwtUtil;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.AuthRequest;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.AuthResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            final String jwt = jwtUtil.generateToken(authentication.getName());
            return ResponseEntity.ok(new AuthResponse(jwt));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Usuario o contrasenia incorrectos.");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Error de autenticacion LDAP: " + e.getMessage());
        }
    }
}