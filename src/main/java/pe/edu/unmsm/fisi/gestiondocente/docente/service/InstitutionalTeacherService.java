package pe.edu.unmsm.fisi.gestiondocente.docente.service;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.CurrentAccountService;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.AccountStatus;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.TeacherRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.InstitutionalCertificateService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.InstitutionalCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.cargadocente.repository.AcademicWorkloadRepository;

@Service
@Profile("!demo & !test")
@Transactional(readOnly = true)
public class InstitutionalTeacherService {
    private final TeacherRepository teachers;
    private final InstitutionalAccountRepository accounts;
    private final CurrentAccountService identity;
    private final InstitutionalCertificateService certificates;
    private final AcademicWorkloadRepository workloads;
    public InstitutionalTeacherService(TeacherRepository teachers, InstitutionalAccountRepository accounts,
            CurrentAccountService identity, InstitutionalCertificateService certificates, AcademicWorkloadRepository workloads) {
        this.teachers = teachers; this.accounts = accounts; this.identity = identity;
        this.certificates = certificates; this.workloads = workloads;
    }
    public record TeacherData(Long id, String codigo, String teacherCode, String nombres, String apellidos,
            String correoInstitucional, String departamentoAcademico, String categoria, String condicion,
            String estado, Long moodleId) {}
    public record Profile(TeacherData docente, List<InstitutionalCertificateResponse> constancias) {}
    public record Workload(Long academicWorkloadId, String teacherCode, String academicPeriod, String courseCode,
            String courseName, Integer cycle, Integer section, String school, Integer plan, Long moodleId) {}
    public Profile profile(String code, Authentication auth) {
        identity.requireTeacherAccess(auth, code);
        return new Profile(data(find(code)), certificates.list(code, auth));
    }
    public List<TeacherData> directory(String department, Authentication auth) {
        identity.requireManagement(auth);
        return teachers.findAll().stream()
                .filter(t -> department == null || department.isBlank() || (t.getDepartment() != null && t.getDepartment().name().equals(department)))
                .map(this::data).toList();
    }
    public List<Workload> workload(String code, Authentication auth) {
        identity.requireTeacherAccess(auth, code);
        find(code);
        return workloads.findByTeacherCodeOrderById(code).stream().map(w -> new Workload(w.getId(), code,
                w.getAcademicPeriod().getSemesterCode(), w.getCourse().getCode(), w.getCourse().getName(),
                w.getCycle(), w.getSection(), w.getSchool().name(), w.getPlan(), w.getMoodleId())).toList();
    }
    private Teacher find(String code) {
        return teachers.findByCode(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Docente no encontrado"));
    }
    private TeacherData data(Teacher t) {
        var person = t.getPerson();
        String email = accounts.findByPersonIdOrderByMainDescIdAsc(person.getId()).stream()
                .filter(a -> a.getAccountStatus() == AccountStatus.ACTIVO).map(a -> a.getInstitutionalEmail()).findFirst().orElse("");
        return new TeacherData(t.getId(), t.getCode(), t.getCode(), person.getFirstName(),
                person.getPaternalLastName() + (person.getMaternalLastName() == null ? "" : " " + person.getMaternalLastName()),
                email, t.getDepartment() == null ? "" : t.getDepartment().name(), "No registrado", "No registrado",
                person.getRegisterState() == null ? "No registrado" : person.getRegisterState().name(), t.getMoodleId());
    }
}
