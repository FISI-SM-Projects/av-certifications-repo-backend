package pe.edu.unmsm.fisi.gestiondocente.docente.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.AccountStatus;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.AuthenticatedAccount;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.InstitutionalAccount;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.CurrentAccountService;
import pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity.AcademicWorkload;
import pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity.School;
import pe.edu.unmsm.fisi.gestiondocente.cargadocente.repository.AcademicWorkloadRepository;
import pe.edu.unmsm.fisi.gestiondocente.curso.entity.Course;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Department;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.TeacherRepository;
import pe.edu.unmsm.fisi.gestiondocente.periodo.entity.AcademicPeriod;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;

class TeacherApiServiceTest {
    private final CurrentAccountService identity = mock(CurrentAccountService.class);
    private final TeacherRepository teachers = mock(TeacherRepository.class);
    private final InstitutionalAccountRepository accounts = mock(InstitutionalAccountRepository.class);
    private final AcademicWorkloadRepository workloads = mock(AcademicWorkloadRepository.class);
    private final TeacherApiService service = new TeacherApiService(identity, teachers, accounts, workloads);

    @Test
    void mapsCurrentTeacherToApidogShape() {
        Fixtures f = fixtures();
        when(identity.account(any())).thenReturn(f.account());
        when(teachers.findByPersonId(10L)).thenReturn(Optional.of(f.teacher()));
        when(accounts.findByPersonIdOrderByMainDescIdAsc(10L)).thenReturn(List.of(f.account()));

        var response = service.me(authentication());

        assertThat(response.code()).isEqualTo("22200101");
        assertThat(response.personId()).isEqualTo(10L);
        assertThat(response.email()).isEqualTo("lalarconl@unmsm.edu.pe");
        assertThat(response.firstName()).isEqualTo("LUIS ALBERTO");
        assertThat(response.department()).isEqualTo("CC");
    }

    @Test
    void coursesApplyFiltersAndPagination() {
        Fixtures f = fixtures();
        when(identity.account(any())).thenReturn(f.account());
        when(teachers.findByPersonId(10L)).thenReturn(Optional.of(f.teacher()));
        when(workloads.findByTeacherCodeOrderById("22200101")).thenReturn(List.of(
                workload(f.teacher(), 1L, "202W0701", "Ingeniería de Software I", "26.1", 8, 2018),
                workload(f.teacher(), 2L, "202W0702", "Bases de Datos I", "26.1", 9, 2018),
                workload(f.teacher(), 3L, "202W0703", "Arquitectura de Software", "26.2", 9, 2022)
        ));

        var page = service.courses(authentication(), "26.1", null, 2018, "software", 0, 1);

        assertThat(page.data()).extracting(item -> item.course().code()).containsExactly("202W0701");
        assertThat(page.pagination().totalElements()).isEqualTo(1);
        assertThat(page.pagination().pageSize()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidPageAndExcessiveSize() {
        assertThatThrownBy(() -> service.courses(authentication(), null, null, null, null, -1, 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("page");
        assertThatThrownBy(() -> service.courses(authentication(), null, null, null, null, 0, TeacherApiService.MAX_SIZE + 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("size");
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken(new AuthenticatedAccount("lalarconl", 1L), null, List.of());
    }

    private Fixtures fixtures() {
        Person person = new Person();
        set(person, "id", 10L);
        set(person, "firstName", "LUIS ALBERTO");
        set(person, "paternalLastName", "ALARCON");
        set(person, "maternalLastName", "LOAYZA");
        set(person, "dni", "22233344");
        set(person, "registerState", AccountStatus.ACTIVO);

        Teacher teacher = new Teacher();
        teacher.setId(2L);
        teacher.setCode("22200101");
        teacher.setMoodleId(46L);
        teacher.setDepartment(Department.CC);
        teacher.setPerson(person);

        InstitutionalAccount account = new InstitutionalAccount();
        set(account, "id", 1L);
        set(account, "ldapUid", "lalarconl");
        set(account, "institutionalEmail", "lalarconl@unmsm.edu.pe");
        set(account, "accountStatus", AccountStatus.ACTIVO);
        set(account, "person", person);
        return new Fixtures(person, teacher, account);
    }

    private AcademicWorkload workload(Teacher teacher, Long id, String code, String name, String semester,
            int cycle, int plan) {
        Course course = new Course();
        course.setId(id);
        course.setCode(code);
        course.setName(name);

        AcademicPeriod period = new AcademicPeriod();
        period.setId(id);
        period.setSemesterCode(semester);
        period.setStartDate(LocalDate.of(2026, 3, 15));
        period.setEndDate(LocalDate.of(2026, 7, 20));

        AcademicWorkload workload = new AcademicWorkload();
        workload.setId(id);
        workload.setMoodleId(100L + id);
        workload.setTeacher(teacher);
        workload.setCourse(course);
        workload.setAcademicPeriod(period);
        workload.setCycle(cycle);
        workload.setSection(1);
        workload.setPlan(plan);
        workload.setSchool(School.SW);
        return workload;
    }

    private record Fixtures(Person person, Teacher teacher, InstitutionalAccount account) {
    }

    private static void set(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("No se pudo preparar el fixture " + fieldName, exception);
        }
    }
}
