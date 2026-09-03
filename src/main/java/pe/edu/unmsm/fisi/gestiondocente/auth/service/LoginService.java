package pe.edu.unmsm.fisi.gestiondocente.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.LoginRequest;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.LoginResponse;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.InstitutionalAccount;
import pe.edu.unmsm.fisi.gestiondocente.auth.exception.InvalidCredentialsException;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final AuthenticationManager authenticationManager;
    private final InstitutionalAccountRepository accountRepository;
    private final JwtService jwtService;

    public LoginService(
            AuthenticationManager authenticationManager,
            InstitutionalAccountRepository accountRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.accountRepository = accountRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de inicio de sesión no puede ser nula");
        }

        String username = request.username() != null ? request.username().trim() : "";
        String password = request.password() != null ? request.password() : "";

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            InstitutionalAccount account = accountRepository.findByLdapUid(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no registrado en el sistema"));

            if (!account.isAccountNonLocked()) {
                throw new LockedException("La cuenta de usuario se encuentra bloqueada");
            }

            if (!account.isEnabled()) {
                throw new DisabledException("La cuenta o el registro de persona se encuentra inactivo");
            }

            List<String> roles = account.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("roles", roles);

            if (account.getPerson() != null){
                extraClaims.put("personId", account.getPerson().getId());
            }

            String token = jwtService.generateToken(extraClaims, account.getUsername());

            return new LoginResponse(token);

        } catch (UsernameNotFoundException e) {
            log.warn("Cuenta no registrada en el sistema {} ", username);
            throw new InvalidCredentialsException("Usuario no registrado en el sistema", e);
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
