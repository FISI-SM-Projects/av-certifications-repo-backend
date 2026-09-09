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
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.*;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.*;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.CertificationRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.pdf.PdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.AccountStatus;

@Service
@Profile("!demo & !test")
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
        var rows = certificates.findByAcademicWorkloadTeacherCodeOrderByIdDesc(code);
        Map<Long, Integer> versions = new HashMap<>();
        rows.forEach(c -> versions.merge(c.getAcademicWorkload().getId(), 1, Integer::sum));
        return rows.stream().map(c -> {
            Long workloadId = c.getAcademicWorkload().getId();
            int version = versions.get(workloadId);
            versions.put(workloadId, version - 1);
            return response(c, version);
        }).toList();
    }
    public InstitutionalCertificateResponse detail(String id, Authentication auth) { return response(find(id, auth)); }
    public byte[] readPdf(String id, Authentication auth) { return storage.read(find(id, auth).getDocumentPath()); }
    public List<InstitutionalCertificateResponse> history(String key, Authentication auth) {
        if (!key.startsWith("workload-")) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Constancia no encontrada");
        var workload = workloads.findById(parseId(key.substring(9)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carga no encontrada"));
        identity.requireTeacherAccess(auth, workload.getTeacher().getCode());
        return certificates.findByAcademicWorkloadIdOrderById(workload.getId()).stream().map(this::response).toList();
    }
    private Certification find(String id, Authentication auth) {
        var cert = certificates.findById(parseId(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Constancia no encontrada"));
        identity.requireTeacherAccess(auth, cert.getAcademicWorkload().getTeacher().getCode());
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
    private InstitutionalCertificateResponse response(Certification c) {
        var w = c.getAcademicWorkload();
        var history = certificates.findByAcademicWorkloadIdOrderById(w.getId());
        int version = 1;
        for (int i = 0; i < history.size(); i++) if (history.get(i).getId().equals(c.getId())) { version = i + 1; break; }
        return response(c, version);
    }
    private InstitutionalCertificateResponse response(Certification c, int version) {
        var w = c.getAcademicWorkload();
        String url = "/api/v1/constancias/generaciones/" + c.getId();
        return new InstitutionalCertificateResponse(c.getId().toString(), "workload-" + w.getId(), version, "CURSO",
                c.getStatus() == null ? "NO_EMITIDO" : c.getStatus().name(), w.getTeacher().getCode(),
                w.getCourse().getCode(), w.getSection().toString(), w.getAcademicPeriod().getSemesterCode(),
                c.getCreatedAt() == null ? null : c.getCreatedAt().atZone(LIMA).toInstant(),
                url + "/pdf", url + "/download", w.getCourse().getName(), w.getTeacher().getPerson().getFullName(),
                storage.available(c.getDocumentPath()));
    }
}
