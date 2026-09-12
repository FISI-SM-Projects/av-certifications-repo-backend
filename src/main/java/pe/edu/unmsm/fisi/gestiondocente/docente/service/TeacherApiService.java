package pe.edu.unmsm.fisi.gestiondocente.docente.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.AccountStatus;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.CurrentAccountService;
import pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity.AcademicWorkload;
import pe.edu.unmsm.fisi.gestiondocente.cargadocente.repository.AcademicWorkloadRepository;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.api.TeacherCourseResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.api.TeacherMeResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.TeacherRepository;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.PaginatedResponse;

@Service
@Profile("!test")
@Transactional(readOnly = true)
public class TeacherApiService {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 50;

    private final CurrentAccountService identity;
    private final TeacherRepository teachers;
    private final InstitutionalAccountRepository accounts;
    private final AcademicWorkloadRepository workloads;

    public TeacherApiService(CurrentAccountService identity, TeacherRepository teachers,
            InstitutionalAccountRepository accounts, AcademicWorkloadRepository workloads) {
        this.identity = identity;
        this.teachers = teachers;
        this.accounts = accounts;
        this.workloads = workloads;
    }

    public TeacherMeResponse me(Authentication authentication) {
        return teacherMe(currentTeacher(authentication));
    }

    public CoursePage courses(Authentication authentication, String semester, Integer cycle, Integer plan,
            String course, Integer page, Integer size) {
        int pageNumber = normalizePage(page);
        int pageSize = normalizeSize(size);
        Teacher teacher = currentTeacher(authentication);

        List<TeacherCourseResponse> filtered = workloads.findByTeacherCodeOrderById(teacher.getCode()).stream()
                .filter(workload -> matchesSemester(workload, semester))
                .filter(workload -> cycle == null || cycle.equals(workload.getCycle()))
                .filter(workload -> plan == null || plan.equals(workload.getPlan()))
                .filter(workload -> matchesCourse(workload, course))
                .map(this::course)
                .toList();

        long totalElements = filtered.size();
        int fromIndex = Math.min(pageNumber * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        List<TeacherCourseResponse> data = filtered.subList(fromIndex, toIndex);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);

        var pagination = new PaginatedResponse.Pagination(pageNumber, pageSize, totalElements, totalPages, data.size());
        return new CoursePage(data, pagination);
    }

    private Teacher currentTeacher(Authentication authentication) {
        var account = identity.account(authentication);
        return teachers.findByPersonId(account.getPerson().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher profile not found"));
    }

    private TeacherMeResponse teacherMe(Teacher teacher) {
        var person = teacher.getPerson();
        String email = accounts.findByPersonIdOrderByMainDescIdAsc(person.getId()).stream()
                .filter(account -> account.getAccountStatus() == AccountStatus.ACTIVO)
                .map(account -> account.getInstitutionalEmail())
                .findFirst()
                .orElse("");
        return new TeacherMeResponse(
                teacher.getId(),
                person.getId(),
                teacher.getMoodleId(),
                teacher.getCode(),
                person.getDni(),
                email,
                person.getFirstName(),
                person.getPaternalLastName(),
                person.getMaternalLastName(),
                teacher.getDepartment() == null ? null : teacher.getDepartment().name(),
                person.getRegisterState() == null ? null : person.getRegisterState().name()
        );
    }

    private TeacherCourseResponse course(AcademicWorkload workload) {
        var course = workload.getCourse();
        var period = workload.getAcademicPeriod();
        return new TeacherCourseResponse(
                workload.getId(),
                workload.getMoodleId(),
                workload.getTeacher().getId(),
                workload.getCycle(),
                workload.getSection(),
                workload.getPlan(),
                workload.getSchool().name(),
                new TeacherCourseResponse.CourseSummary(course.getId(), course.getCode(), course.getName()),
                new TeacherCourseResponse.AcademicPeriodSummary(period.getId(), period.getSemesterCode(),
                        atStartOfDay(period.getStartDate()), atStartOfDay(period.getEndDate()))
        );
    }

    private boolean matchesSemester(AcademicWorkload workload, String semester) {
        return semester == null || semester.isBlank()
                || workload.getAcademicPeriod().getSemesterCode().equalsIgnoreCase(semester.trim());
    }

    private boolean matchesCourse(AcademicWorkload workload, String course) {
        if (course == null || course.isBlank()) {
            return true;
        }
        String expected = course.trim().toLowerCase(Locale.ROOT);
        return workload.getCourse().getCode().toLowerCase(Locale.ROOT).contains(expected)
                || workload.getCourse().getName().toLowerCase(Locale.ROOT).contains(expected);
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be greater than or equal to 0");
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be greater than or equal to 1");
        }
        if (size > MAX_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be less than or equal to " + MAX_SIZE);
        }
        return size;
    }

    private OffsetDateTime atStartOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    public record CoursePage(List<TeacherCourseResponse> data, PaginatedResponse.Pagination pagination) {
    }
}
