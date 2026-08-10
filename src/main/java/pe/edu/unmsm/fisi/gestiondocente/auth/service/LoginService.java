package pe.edu.unmsm.fisi.gestiondocente.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.auth.dto.LoginRequest;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.LoginResponse;
import pe.edu.unmsm.fisi.gestiondocente.auth.exception.InvalidCredentialsException;

@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de inicio de sesión no puede ser nula");
        }

        String username = request.username() != null ? request.username().trim() : "";
        String password = request.password() != null ? request.password() : "";

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            String token = jwtService.generateToken(authentication.getName());
            return new LoginResponse(token);

        } catch (BadCredentialsException e) {
            log.warn("Credenciales inválidas para el usuario: {}", username);
            throw new InvalidCredentialsException("Usuario o contraseña incorrectos", e);
        } catch (DisabledException e) {
            log.warn("Cuenta deshabilitada para el usuario: {}", username);
            throw new InvalidCredentialsException("La cuenta de usuario se encuentra deshabilitada", e);
        } catch (LockedException e) {
            log.warn("Cuenta bloqueada para el usuario: {}", username);
            throw new InvalidCredentialsException("La cuenta de usuario se encuentra bloqueada", e);
        } catch (AuthenticationException e) {
            log.error("Error durante la autenticación LDAP para el usuario: {}", username, e);
            throw new InvalidCredentialsException("Error de autenticación: credenciales no válidas", e);
        } catch (Exception e) {
            log.error("Error inesperado en el inicio de sesión para el usuario: {}", username, e);
            throw new RuntimeException("Error inesperado al procesar el inicio de sesión", e);
        }
    }
}
