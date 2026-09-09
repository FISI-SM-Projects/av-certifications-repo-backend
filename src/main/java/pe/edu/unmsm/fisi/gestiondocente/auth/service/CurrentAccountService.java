package pe.edu.unmsm.fisi.gestiondocente.auth.service;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.CurrentUserResponse;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.*;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.TeacherRepository;

@Service
@Profile("!test")
@Transactional(readOnly = true)
public class CurrentAccountService {
    private final InstitutionalAccountRepository accounts;
    private final TeacherRepository teachers;
    public CurrentAccountService(InstitutionalAccountRepository accounts, TeacherRepository teachers) {
        this.accounts = accounts;
        this.teachers = teachers;
    }
    public InstitutionalAccount account(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticacion requerida");
        }
        var found = auth.getPrincipal() instanceof AuthenticatedAccount principal && principal.accountId() != null
                ? accounts.findById(principal.accountId()) : accounts.findByLdapUid(auth.getName());
        var account = found.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cuenta no registrada"));
        if (!account.getUsername().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identidad de cuenta invalida");
        }
        if (!account.isEnabled() || !account.isAccountNonLocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta o persona inactiva");
        }
        return account;
    }
    public CurrentUserResponse me(Authentication auth) {
        var a = account(auth);
        var t = teachers.findByPersonId(a.getPerson().getId()).orElse(null);
        return new CurrentUserResponse(a.getId(), a.getPerson().getId(), a.getUsername(),
                a.getInstitutionalEmail(), a.getPerson().getFullName(),
                a.getAuthorities().stream().map(r -> r.getAuthority().replaceFirst("^ROLE_", "")).sorted().toList(),
                a.getAccountStatus().name(), a.getPerson().getRegisterState().name(),
                t == null ? null : new CurrentUserResponse.TeacherContext(t.getId(), t.getCode(), t.getMoodleId(),
                        t.getDepartment() == null ? null : t.getDepartment().name()), null, null);
    }
    public void requireTeacherAccess(Authentication auth, String code) {
        var user = me(auth);
        if (user.roles().contains("ADMIN") || user.roles().contains("DIRECTOR_ESCUELA")) return;
        if (!user.roles().contains("DOCENTE") || user.teacher() == null || !user.teacher().teacherCode().equals(code)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para consultar este docente");
        }
    }
    public void requireManagement(Authentication auth) {
        var user = me(auth);
        if (!user.roles().contains("ADMIN") && !user.roles().contains("DIRECTOR_ESCUELA")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para consultar docentes");
        }
    }
}
