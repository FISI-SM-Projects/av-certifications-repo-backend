package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import java.time.*;
import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.CurrentAccountService;
import pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity.AcademicWorkload;
import pe.edu.unmsm.fisi.gestiondocente.cargadocente.repository.AcademicWorkloadRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.InstitutionalCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.SemesterCertificateSource;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.SemesterCertificateSourceSummary;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.*;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.*;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.CertificationRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.pdf.PdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.AccountStatus;

@Service
@Profile("!test")
@Transactional(readOnly = true)
public class InstitutionalCertificateService {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private final CertificationRepository certificates;
    private final AcademicWorkloadRepository workloads;
    private final CurrentAccountService identity;
    private final InstitutionalAccountRepository accounts;
    private final InstitutionalPdfStorage storage;
    private final PdfGenerationService pdf;
    public InstitutionalCertificateService(CertificationRepository certificates, AcademicWorkloadRepository workloads,
            CurrentAccountService identity, InstitutionalAccountRepository accounts, InstitutionalPdfStorage storage, PdfGenerationService pdf) {
        this.certificates = certificates; this.workloads = workloads; this.identity = identity;
        this.accounts = accounts; this.storage = storage; this.pdf = pdf;
    }
    public List<InstitutionalCertificateResponse> list(String code, Authentication auth) {
        identity.requireTeacherAccess(auth, code);
        return certificates.findByTeacherCodeOrderByIdDesc(code).stream().map(this::response).toList();
    }
    public InstitutionalCertificateResponse detail(String id, Authentication auth) { return response(find(id, auth)); }
    public byte[] readPdf(String id, Authentication auth) { return storage.read(find(id, auth).getDocumentPath()); }
    public List<InstitutionalCertificateResponse> history(String key, Authentication auth) {
        if (key.startsWith("workload-")) {
            var workload = workloads.findById(parseId(key.substring(9)))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carga no encontrada"));
            identity.requireTeacherAccess(auth, workload.getTeacher().getCode());
            return certificates.findByAcademicWorkloadIdOrderById(workload.getId()).stream().map(this::response).toList();
        }
        if (key.startsWith("semester-")) {
            String[] parts = key.split("-", 3);
            if (parts.length != 3) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Constancia no encontrada");
            identity.requireTeacherAccess(auth, parts[1]);
            return certificates
                    .findByTeacherCodeAndAcademicPeriodSemesterCodeAndCertificateTypeOrderById(
                            parts[1], parts[2], CertificationType.SEMESTER)
                    .stream().map(this::response).toList();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Constancia no encontrada");
    }
    private Certification find(String id, Authentication auth) {
        var cert = certificates.findById(parseId(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Constancia no encontrada"));
        identity.requireTeacherAccess(auth, teacherCode(cert));
        return cert;
    }
    private Long parseId(String id) {
        try { long number = Long.parseLong(id); if (number <= 0) throw new NumberFormatException(); return number; }
        catch (NumberFormatException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Identificador no encontrado"); }
    }
    public record GenerateRequest(Long academicWorkloadId, TeacherPayload teacher, CoursePayload course, IssuerPayload issuer) {}
    @Transactional
    public InstitutionalCertificateResponse generate(GenerateRequest input, Authentication auth) {
        if (input == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud requerida");
        Long workloadId = input.academicWorkloadId();
        if (workloadId == null) {
            if (input.teacher() == null || input.course() == null || input.teacher().getTeacherCode() == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique la carga academica");
            var matches = workloads.findByTeacherCodeOrderById(input.teacher().getTeacherCode().trim()).stream()
                .filter(w -> w.getCourse().getCode().equals(input.course().getCode())
                        && w.getSection().toString().equals(input.course().getSection())
                        && w.getAcademicPeriod().getSemesterCode().equals(input.course().getSemester())).toList();
            if (matches.size() != 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud no identifica una carga academica unica");
            workloadId = matches.get(0).getId();
        }
        var w = workloads.findLockedById(workloadId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carga academica no encontrada"));
        identity.requireTeacherAccess(auth, w.getTeacher().getCode());
        var actor = identity.account(auth);
        var teacher = w.getTeacher();
        var account = accounts.findByPersonIdOrderByMainDescIdAsc(teacher.getPerson().getId()).stream()
                .filter(a -> a.getAccountStatus() == AccountStatus.ACTIVO).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Docente sin cuenta institucional activa"));
        if (input.teacher() != null && (!teacher.getCode().equals(input.teacher().getTeacherCode())
                || !teacher.getPerson().getFullName().equalsIgnoreCase(Objects.toString(input.teacher().getFullName(), "").trim())
                || !account.getInstitutionalEmail().equalsIgnoreCase(Objects.toString(input.teacher().getEmail(), "").trim()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La identidad no coincide con el docente registrado");
        }
        var existing = certificates.findByAcademicWorkloadIdOrderById(w.getId());
        if (existing.stream().anyMatch(c -> c.getStatus() == CertificationStatus.VERIFICADO))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una constancia verificada para esta carga");
        var now = LocalDateTime.now(LIMA);
        var cert = new Certification();
        cert.setAcademicWorkload(w); cert.setDocumentPath(storage.newDocumentPath());
        cert.setTeacher(teacher); cert.setAcademicPeriod(w.getAcademicPeriod()); cert.setCertificateType(CertificationType.COURSE);
        cert.setStatus(CertificationStatus.EMITIDO); cert.setCreatedAt(now); cert.setUpdatedAt(now);
        certificates.saveAndFlush(cert);
        var request = new CourseCertificateRequest(
                new TeacherPayload(teacher.getPerson().getFullName(), account.getInstitutionalEmail(), teacher.getCode()),
                new CoursePayload(w.getCourse().getCode(), w.getCourse().getName(), w.getCycle().toString(),
                        w.getSection().toString(), w.getSchool().name(), w.getPlan().toString(), w.getAcademicPeriod().getSemesterCode()),
                new IssuerPayload("FISI", actor.getId().toString(), actor.getInstitutionalEmail()));
        var metadata = new CertificateGenerationMetadata(cert.getId().toString(), "workload-" + w.getId(),
                existing.size() + 1, TipoConstancia.CURSO, EstadoConstancia.GENERADO, teacher.getCode(),
                w.getCourse().getCode(), w.getSection().toString(), w.getAcademicPeriod().getSemesterCode(),
                now.atZone(LIMA).toInstant(), "request.json", "certificate.pdf");
        byte[] bytes = pdf.generateCourseCertificate(request, metadata);
        storage.write(cert.getDocumentPath(), bytes);
        // Una transaccion fallida retira exclusivamente el PDF nuevo; no toca documentos previos.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) storage.removeUncommitted(cert.getDocumentPath());
            }
        });
        return response(cert);
    }
    @Transactional
    public InstitutionalCertificateResponse generateSemester(SemesterCertificateRequest input, Authentication auth) {
        if (input == null || input.getTeacherCode() == null || input.getTeacherCode().isBlank()
                || input.getSemester() == null || input.getSemester().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique docente y periodo academico");
        }
        String teacherCode = input.getTeacherCode().trim();
        String semester = input.getSemester().trim();
        identity.requireTeacherAccess(auth, teacherCode);
        var sourceRows = certificates.findByTeacherCodeAndAcademicPeriodSemesterCodeAndCertificateTypeOrderById(
                teacherCode, semester, CertificationType.COURSE).stream()
                .filter(c -> c.getStatus() == CertificationStatus.EMITIDO || c.getStatus() == CertificationStatus.VERIFICADO)
                .toList();
        if (sourceRows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No existen constancias por curso validas para consolidar este periodo");
        }
        var first = sourceRows.get(0);
        var teacher = first.getTeacher();
        var period = first.getAcademicPeriod();
        var account = accounts.findByPersonIdOrderByMainDescIdAsc(teacher.getPerson().getId()).stream()
                .filter(a -> a.getAccountStatus() == AccountStatus.ACTIVO).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Docente sin cuenta institucional activa"));
        var existing = certificates.findByTeacherIdAndAcademicPeriodIdAndCertificateTypeOrderById(
                teacher.getId(), period.getId(), CertificationType.SEMESTER);
        if (existing.stream().anyMatch(c -> c.getStatus() == CertificationStatus.VERIFICADO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una constancia semestral verificada para este periodo");
        }
        var now = LocalDateTime.now(LIMA);
        var cert = new Certification();
        cert.setCertificateType(CertificationType.SEMESTER); cert.setTeacher(teacher); cert.setAcademicPeriod(period);
        cert.setAcademicWorkload(null); cert.setDocumentPath(storage.newDocumentPath());
        cert.setStatus(CertificationStatus.EMITIDO); cert.setCreatedAt(now); cert.setUpdatedAt(now);
        certificates.saveAndFlush(cert);
        var sourceSummary = new SemesterCertificateSourceSummary(
                teacher.getCode(), teacher.getPerson().getFullName(), account.getInstitutionalEmail(), semester,
                sourceRows.stream().map(this::source).toList());
        var metadata = new CertificateGenerationMetadata(cert.getId().toString(), semesterKey(teacher.getCode(), semester),
                existing.size() + 1, TipoConstancia.SEMESTRAL, EstadoConstancia.GENERADO, teacher.getCode(),
                null, null, semester, now.atZone(LIMA).toInstant(), "request.json", "certificate.pdf");
        byte[] bytes = pdf.generateSemesterCertificate(sourceSummary, metadata);
        storage.write(cert.getDocumentPath(), bytes);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) storage.removeUncommitted(cert.getDocumentPath());
            }
        });
        return response(cert);
    }
    private InstitutionalCertificateResponse response(Certification c) {
        var w = c.getAcademicWorkload();
        var history = c.getCertificateType() == CertificationType.SEMESTER
                ? certificates.findByTeacherIdAndAcademicPeriodIdAndCertificateTypeOrderById(
                        c.getTeacher().getId(), c.getAcademicPeriod().getId(), CertificationType.SEMESTER)
                : certificates.findByAcademicWorkloadIdOrderById(w.getId());
        int version = 1;
        for (int i = 0; i < history.size(); i++) if (history.get(i).getId().equals(c.getId())) { version = i + 1; break; }
        return response(c, version);
    }
    private InstitutionalCertificateResponse response(Certification c, int version) {
        var w = c.getAcademicWorkload();
        var teacher = c.getTeacher() != null ? c.getTeacher() : w.getTeacher();
        var period = c.getAcademicPeriod() != null ? c.getAcademicPeriod() : w.getAcademicPeriod();
        String url = "/api/v1/constancias/generaciones/" + c.getId();
        if (c.getCertificateType() == CertificationType.SEMESTER) {
            return new InstitutionalCertificateResponse(c.getId().toString(), semesterKey(teacher.getCode(), period.getSemesterCode()),
                    version, "SEMESTRAL", "SEMESTER", status(c), teacher.getCode(), null, null, period.getSemesterCode(),
                    c.getCreatedAt() == null ? null : c.getCreatedAt().atZone(LIMA).toInstant(),
                    url + "/pdf", url + "/download", null, teacher.getPerson().getFullName(),
                    storage.available(c.getDocumentPath()));
        }
        return new InstitutionalCertificateResponse(c.getId().toString(), "workload-" + w.getId(), version, "CURSO", "COURSE",
                status(c), teacher.getCode(), w.getCourse().getCode(), w.getSection().toString(), period.getSemesterCode(),
                c.getCreatedAt() == null ? null : c.getCreatedAt().atZone(LIMA).toInstant(),
                url + "/pdf", url + "/download", w.getCourse().getName(), teacher.getPerson().getFullName(),
                storage.available(c.getDocumentPath()));
    }
    private SemesterCertificateSource source(Certification c) {
        var w = c.getAcademicWorkload();
        return new SemesterCertificateSource(c.getId().toString(), "workload-" + w.getId(), w.getCourse().getCode(),
                w.getCourse().getName(), w.getSection().toString(), w.getSchool().name(), w.getPlan().toString(),
                c.getStatus() == CertificationStatus.VERIFICADO ? EstadoConstancia.APROBADO : EstadoConstancia.GENERADO);
    }
    private String teacherCode(Certification c) {
        return c.getTeacher() != null ? c.getTeacher().getCode() : c.getAcademicWorkload().getTeacher().getCode();
    }
    private String semesterKey(String teacherCode, String semester) { return "semester-" + teacherCode + "-" + semester; }
    private String status(Certification c) { return c.getStatus() == null ? "NO_EMITIDO" : c.getStatus().name(); }
}
