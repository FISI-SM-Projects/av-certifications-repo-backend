package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.unmsm.fisi.gestiondocente.auth.dto.UserPrincipal;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.InstitutionalAccount;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.AuthenticatedTeacherContext;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;
import pe.edu.unmsm.fisi.gestiondocente.teacher.entity.Department;
import pe.edu.unmsm.fisi.gestiondocente.teacher.entity.Teacher;
import pe.edu.unmsm.fisi.gestiondocente.teacher.repository.TeacherRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticatedTeacherContextServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private InstitutionalAccountRepository institutionalAccountRepository;

    @InjectMocks
    private AuthenticatedTeacherContextService service;

    @Test
    void resolvesTeacherUsingOnlyAuthenticatedPrincipal() {
        Person person = Person.builder()
                .id(41L)
                .firstName("Docente")
                .paternalLastName("Prueba")
                .maternalLastName("Sistema")
                .build();
        Teacher teacher = Teacher.builder()
                .id(8L)
                .person(person)
                .code("DEV001")
                .department(Department.SW)
                .build();
        InstitutionalAccount account = InstitutionalAccount.builder()
                .person(person)
                .ldapUid("docente.dev")
                .institutionalEmail("docente.dev@example.test")
                .build();
        UserPrincipal principal = new UserPrincipal("docente.dev", 41L);

        when(teacherRepository.findByPersonId(41L)).thenReturn(Optional.of(teacher));
        when(institutionalAccountRepository.findByLdapUid("docente.dev")).thenReturn(Optional.of(account));

        AuthenticatedTeacherContext context = service.getContext(principal);

        assertThat(context.teacherCode()).isEqualTo("DEV001");
        assertThat(context.username()).isEqualTo("docente.dev");
        assertThat(context.fullName()).isEqualTo("Docente Prueba Sistema");
        verify(teacherRepository).findByPersonId(principal.personId());
        verify(institutionalAccountRepository).findByLdapUid(principal.username());
    }
}
