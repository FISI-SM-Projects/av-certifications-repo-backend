package pe.edu.unmsm.fisi.gestiondocente.auth.service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.*;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.*;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.TeacherRepository;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;

class CurrentAccountServiceTest {
    InstitutionalAccountRepository accounts = mock(InstitutionalAccountRepository.class);
    TeacherRepository teachers = mock(TeacherRepository.class);
    CurrentAccountService service = new CurrentAccountService(accounts, teachers);
    InstitutionalAccount account = mock(InstitutionalAccount.class);
    Person person = mock(Person.class);
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(new AuthenticatedAccount("realuser", 7L), null, List.of());
    @BeforeEach void setup() {
        when(accounts.findById(7L)).thenReturn(Optional.of(account));
        when(account.getUsername()).thenReturn("realuser"); when(account.getId()).thenReturn(7L);
        when(account.isEnabled()).thenReturn(true); when(account.isAccountNonLocked()).thenReturn(true);
        when(account.getPerson()).thenReturn(person); when(person.getId()).thenReturn(9L);
        when(person.getFullName()).thenReturn("Nombre Institucional");
        when(account.getInstitutionalEmail()).thenReturn("real@unmsm.edu.pe");
        when(account.getAccountStatus()).thenReturn(AccountStatus.ACTIVO);
        when(person.getRegisterState()).thenReturn(AccountStatus.ACTIVO);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_DOCENTE"))).when(account).getAuthorities();
        when(teachers.findByPersonId(9L)).thenReturn(Optional.empty());
    }
    @Test void returnsRealIdentityAndStringTeacherCode() {
        var teacher = new Teacher(); teacher.setId(11L); teacher.setCode("00112233");
        teacher.setMoodleId(4_000_000_000L); teacher.setDepartment(Department.CC);
        when(teachers.findByPersonId(9L)).thenReturn(Optional.of(teacher));
        var me = service.me(auth);
        assertEquals("00112233", me.teacher().teacherCode());
        assertEquals("Nombre Institucional", me.fullName()); assertEquals(List.of("DOCENTE"), me.roles());
        assertEquals(4_000_000_000L, me.teacher().moodleId());
    }
    @Test void accountWithoutTeacherHasNoInventedProfile() {
        var me = service.me(auth);
        assertNull(me.teacher()); assertNull(me.student()); assertNull(me.administrative());
    }
    @Test void rejectsAnonymous() { assertEquals(401, assertThrows(ResponseStatusException.class, () -> service.me(null)).getStatusCode().value()); }
    @Test void rejectsInactiveAccount() {
        when(account.isEnabled()).thenReturn(false);
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.me(auth)).getStatusCode().value());
    }
    @Test void rejectsAccountSubjectMismatch() {
        when(account.getUsername()).thenReturn("different");
        assertEquals(401, assertThrows(ResponseStatusException.class, () -> service.me(auth)).getStatusCode().value());
    }
    @Test void subjectFallbackUsesRepository() {
        when(accounts.findByLdapUid("realuser")).thenReturn(Optional.of(account));
        service.me(new UsernamePasswordAuthenticationToken("realuser", null, List.of()));
        verify(accounts).findByLdapUid("realuser");
    }
    @Test void cannotReadAnotherTeacher() {
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.requireTeacherAccess(auth, "22200101")).getStatusCode().value());
    }
    @Test void rolesAreReadFromCurrentDatabaseState() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(account).getAuthorities();
        assertDoesNotThrow(() -> service.requireTeacherAccess(auth, "22200101"));
    }
}
