package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.unmsm.fisi.gestiondocente.auth.dto.UserPrincipal;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.InstitutionalAccount;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.AuthenticatedTeacherContext;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.AuthenticatedTeacherContextNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;
import pe.edu.unmsm.fisi.gestiondocente.teacher.entity.Teacher;
import pe.edu.unmsm.fisi.gestiondocente.teacher.repository.TeacherRepository;

@Service
@RequiredArgsConstructor
public class AuthenticatedTeacherContextService {

    private final TeacherRepository teacherRepository;
    private final InstitutionalAccountRepository institutionalAccountRepository;

    @Transactional(readOnly = true)
    public AuthenticatedTeacherContext getContext(UserPrincipal principal) {
        if (principal == null || principal.personId() == null || principal.username() == null) {
            throw new AuthenticatedTeacherContextNotFoundException();
        }

        Teacher teacher = teacherRepository.findByPersonId(principal.personId())
                .orElseThrow(AuthenticatedTeacherContextNotFoundException::new);
        InstitutionalAccount account = institutionalAccountRepository.findByLdapUid(principal.username())
                .filter(candidate -> candidate.getPerson() != null)
                .filter(candidate -> principal.personId().equals(candidate.getPerson().getId()))
                .orElseThrow(AuthenticatedTeacherContextNotFoundException::new);
        Person person = teacher.getPerson();

        return new AuthenticatedTeacherContext(
                teacher.getId(),
                teacher.getCode(),
                principal.username(),
                account.getInstitutionalEmail(),
                person.getFirstName(),
                person.getPaternalLastName(),
                person.getMaternalLastName(),
                teacher.getDepartment() == null ? null : teacher.getDepartment().name());
    }
}
