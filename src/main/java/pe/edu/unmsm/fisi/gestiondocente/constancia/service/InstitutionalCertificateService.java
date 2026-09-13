package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.api.CertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.api.CreateCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.*;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.*;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.IncompleteSemesterSourceException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.CertificationRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.pdf.PdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.AccountStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.PaginatedResponse;

@Service
@Profile("!test")
@Transactional(readOnly = true)
public class InstitutionalCertificateService {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
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
    public CertificatePage listApi(String teacherCode, String certificateType, String status, String semester,
            String course, Integer page, Integer size, Authentication auth) {
        int pageNumber = normalizePage(page);
        int pageSize = normalizeSize(size);
        var user = identity.me(auth);
        List<Certification> rows;
        if (teacherCode != null && !teacherCode.isBlank()) {
            identity.requireTeacherAccess(auth, teacherCode.trim());
            rows = certificates.findByTeacherCodeOrderByIdDesc(teacherCode.trim());
        } else if (user.teacher() != null && user.roles().contains("DOCENTE") && !user.roles().contains("ADMIN")) {
            rows = certificates.findByTeacherCodeOrderByIdDesc(user.teacher().teacherCode());
        } else {
            identity.requireManagement(auth);
            rows = certificates.findAllByOrderByIdDesc().stream()
                    .filter(c -> canReadTeacher(auth, teacherCode(c)))
                    .toList();
        }
        var filtered = latestVisibleCertificates(rows).stream()
                .filter(c -> matches(c, certificateType, status, semester, course))
                .toList();
        int totalElements = filtered.size();
        int from = Math.min(pageNumber * pageSize, totalElements);
        int to = Math.min(from + pageSize, totalElements);
        var data = filtered.subList(from, to).stream().map(this::apiResponse).toList();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);
        return new CertificatePage(data, new PaginatedResponse.Pagination(pageNumber, pageSize, totalElements, totalPages, data.size()));
    }

    @Transactional
    public CertificateResponse createApi(CreateCertificateRequest input, Authentication auth) {
        if (input == null || input.certificateType() == null || input.certificateType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique el tipo de constancia");
        }
        var type = certificateType(input.certificateType());
        if (type == CertificationType.COURSE) {
            if (input.academicWorkloadId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique la carga academica");
            }
            var generated = generate(new GenerateRequest(input.academicWorkloadId(), null, null, null), auth);
            return detailApi(generated.generationId(), auth);
        }
        String teacherCode = Objects.toString(input.teacherCode(), "").trim();
        String semester = Objects.toString(input.semester(), "").trim();
        if (teacherCode.isBlank() || semester.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique docente y periodo academico");
        }
        identity.requireCertificateGenerationAccess(auth, teacherCode);
        if (!Boolean.TRUE.equals(input.confirmIncomplete())) {
            requireCompleteSemesterSource(teacherCode, semester);
        }
        var generated = generateSemester(new SemesterCertificateRequest(teacherCode, semester, List.of()), auth);
        return detailApi(generated.generationId(), auth);
    }

    public CertificateResponse detailApi(String id, Authentication auth) {
        return apiResponse(find(id, auth));
    }

    public CertificatePage versionsApi(String id, Integer page, Integer size, Authentication auth) {
        var user = identity.me(auth);
        if (!user.roles().contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN puede consultar historial completo");
        }
        int pageNumber = normalizePage(page);
        int pageSize = normalizeSize(size);
        var cert = certificates.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Constancia no encontrada"));
        var history = historyRows(cert);
        int totalElements = history.size();
        int from = Math.min(pageNumber * pageSize, totalElements);
        int to = Math.min(from + pageSize, totalElements);
        var data = history.subList(from, to).stream().map(this::apiResponse).toList();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);
        return new CertificatePage(data, new PaginatedResponse.Pagination(pageNumber, pageSize, totalElements, totalPages, data.size()));
    }

    @Transactional
    public CertificateResponse signApi(String id, Authentication auth) {
        var signed = sign(id, auth);
        return detailApi(signed.generationId(), auth);
    }

    public List<InstitutionalCertificateResponse> list(String code, Authentication auth) {
        identity.requireTeacherAccess(auth, code);
        return latestVisibleCertificates(certificates.findByTeacherCodeOrderByIdDesc(code)).stream().map(this::response).toList();
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
        identity.requireCertificateGenerationAccess(auth, w.getTeacher().getCode());
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
        String snapshot = courseSnapshot(w);
        String contentHash = contentHash(snapshot);
        var latest = existing.stream().reduce((first, second) -> second);
        if (latest.isPresent() && isSigned(latest.get())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La ultima version de esta constancia ya esta firmada");
        }
        if (latest.isPresent() && isGenerated(latest.get()) && hasSameContent(latest.get(), contentHash)) {
            return response(latest.get());
        }
        int version = nextVersion(existing);
        var now = LocalDateTime.now(LIMA);
        var cert = new Certification();
        cert.setAcademicWorkload(w); cert.setDocumentPath(storage.newDocumentPath());
        cert.setTeacher(teacher); cert.setAcademicPeriod(w.getAcademicPeriod()); cert.setCertificateType(CertificationType.COURSE);
        cert.setStatus(CertificationStatus.GENERADA); cert.setVersionNumber(version);
        cert.setAcademicSnapshotJson(snapshot); cert.setContentHash(contentHash);
        cert.setGeneratedByAccount(actor); cert.setGeneratedAt(now); cert.setCreatedAt(now); cert.setUpdatedAt(now);
        certificates.saveAndFlush(cert);
        var request = new CourseCertificateRequest(
                new TeacherPayload(teacher.getPerson().getFullName(), account.getInstitutionalEmail(), teacher.getCode()),
                new CoursePayload(w.getCourse().getCode(), w.getCourse().getName(), w.getCycle().toString(),
                        w.getSection().toString(), w.getSchool().name(), w.getPlan().toString(), w.getAcademicPeriod().getSemesterCode()),
                new IssuerPayload("FISI", actor.getId().toString(), actor.getInstitutionalEmail()));
        var metadata = new CertificateGenerationMetadata(cert.getId().toString(), "workload-" + w.getId(),
                version, TipoConstancia.CURSO, EstadoConstancia.GENERADO, teacher.getCode(),
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
        identity.requireCertificateGenerationAccess(auth, teacherCode);
        var actor = identity.account(auth);
        var sourceRows = latestCourseCertificatesByWorkload(certificates.findByTeacherCodeAndAcademicPeriodSemesterCodeAndCertificateTypeOrderById(
                teacherCode, semester, CertificationType.COURSE).stream()
                .filter(c -> isGenerated(c) || isSigned(c))
                .toList());
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
        String snapshot = semesterSnapshot(sourceRows);
        String contentHash = contentHash(snapshot);
        var latest = existing.stream().reduce((older, newer) -> newer);
        if (latest.isPresent() && isSigned(latest.get())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La ultima version semestral ya esta firmada");
        }
        if (latest.isPresent() && isGenerated(latest.get()) && hasSameContent(latest.get(), contentHash)) {
            return response(latest.get());
        }
        int version = nextVersion(existing);
        var now = LocalDateTime.now(LIMA);
        var cert = new Certification();
        cert.setCertificateType(CertificationType.SEMESTER); cert.setTeacher(teacher); cert.setAcademicPeriod(period);
        cert.setAcademicWorkload(null); cert.setDocumentPath(storage.newDocumentPath());
        cert.setStatus(CertificationStatus.GENERADA); cert.setVersionNumber(version);
        cert.setAcademicSnapshotJson(snapshot); cert.setContentHash(contentHash);
        cert.setGeneratedByAccount(actor); cert.setGeneratedAt(now); cert.setCreatedAt(now); cert.setUpdatedAt(now);
        certificates.saveAndFlush(cert);
        var sourceSummary = new SemesterCertificateSourceSummary(
                teacher.getCode(), teacher.getPerson().getFullName(), account.getInstitutionalEmail(), semester,
                sourceRows.stream().map(this::source).toList());
        var metadata = new CertificateGenerationMetadata(cert.getId().toString(), semesterKey(teacher.getCode(), semester),
                version, TipoConstancia.SEMESTRAL, EstadoConstancia.GENERADO, teacher.getCode(),
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
    @Transactional
    public InstitutionalCertificateResponse sign(String id, Authentication auth) {
        var cert = certificates.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Constancia no encontrada"));
        identity.requireDirectorSignature(auth, teacherCode(cert));
        if (!isLatestVersion(cert)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se firma la ultima version vigente");
        }
        if (isSigned(cert)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La constancia ya esta firmada");
        }
        if (!isGenerated(cert)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se firman constancias generadas");
        }
        var now = LocalDateTime.now(LIMA);
        cert.setStatus(CertificationStatus.FIRMADA);
        cert.setSignedByAccount(identity.account(auth));
        cert.setSignedAt(now);
        cert.setUpdatedAt(now);
        certificates.saveAndFlush(cert);
        return response(cert);
    }
    private InstitutionalCertificateResponse response(Certification c) {
        var w = c.getAcademicWorkload();
        var history = c.getCertificateType() == CertificationType.SEMESTER
                ? certificates.findByTeacherIdAndAcademicPeriodIdAndCertificateTypeOrderById(
                        c.getTeacher().getId(), c.getAcademicPeriod().getId(), CertificationType.SEMESTER)
                : certificates.findByAcademicWorkloadIdOrderById(w.getId());
        int version = c.getVersionNumber() == null ? 1 : c.getVersionNumber();
        if (c.getVersionNumber() == null) {
            for (int i = 0; i < history.size(); i++) if (history.get(i).getId().equals(c.getId())) { version = i + 1; break; }
        }
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
                    generatedInstant(c),
                    url + "/pdf", url + "/download", null, teacher.getPerson().getFullName(),
                    storage.available(c.getDocumentPath()));
        }
        return new InstitutionalCertificateResponse(c.getId().toString(), "workload-" + w.getId(), version, "CURSO", "COURSE",
                status(c), teacher.getCode(), w.getCourse().getCode(), w.getSection().toString(), period.getSemesterCode(),
                generatedInstant(c),
                url + "/pdf", url + "/download", w.getCourse().getName(), teacher.getPerson().getFullName(),
                storage.available(c.getDocumentPath()));
    }
    private CertificateResponse apiResponse(Certification c) {
        var w = c.getAcademicWorkload();
        var teacher = c.getTeacher() != null ? c.getTeacher() : w.getTeacher();
        var period = c.getAcademicPeriod() != null ? c.getAcademicPeriod() : w.getAcademicPeriod();
        var course = w == null || w.getCourse() == null ? null
                : new CertificateResponse.CourseSummary(w.getCourse().getId(), w.getCourse().getCode(), w.getCourse().getName());
        Integer section = w == null ? null : w.getSection();
        Integer cycle = w == null ? null : w.getCycle();
        Integer plan = w == null ? null : w.getPlan();
        String school = w == null || w.getSchool() == null ? null : w.getSchool().name();
        return new CertificateResponse(
                c.getId(),
                certificateKey(c),
                c.getCertificateType() == null ? CertificationType.COURSE.name() : c.getCertificateType().name(),
                status(c),
                c.getVersionNumber() == null ? computedVersion(c) : c.getVersionNumber(),
                teacher.getId(),
                teacher.getCode(),
                teacher.getPerson().getFullName(),
                period.getId(),
                period.getSemesterCode(),
                w == null ? null : w.getId(),
                course,
                section,
                cycle,
                school,
                plan,
                generatedInstant(c),
                signedInstant(c),
                storage.available(c.getDocumentPath()),
                "/api/v1/certificates/" + c.getId() + "/document");
    }

    private String certificateKey(Certification c) {
        if (c.getCertificateType() == CertificationType.SEMESTER) {
            return semesterKey(c.getTeacher().getCode(), c.getAcademicPeriod().getSemesterCode());
        }
        return "workload-" + c.getAcademicWorkload().getId();
    }

    private SemesterCertificateSource source(Certification c) {
        var w = c.getAcademicWorkload();
        return new SemesterCertificateSource(c.getId().toString(), "workload-" + w.getId(), w.getCourse().getCode(),
                w.getCourse().getName(), w.getSection().toString(), w.getSchool().name(), w.getPlan().toString(),
                isSigned(c) ? EstadoConstancia.APROBADO : EstadoConstancia.GENERADO);
    }
    private List<Certification> latestCourseCertificatesByWorkload(List<Certification> rows) {
        Map<Long, Certification> latestByWorkload = new LinkedHashMap<>();
        for (Certification certification : rows) {
            var workload = certification.getAcademicWorkload();
            if (workload != null && workload.getId() != null) {
                latestByWorkload.put(workload.getId(), certification);
            }
        }
        return new ArrayList<>(latestByWorkload.values());
    }
    private boolean hasSameContent(Certification existing, String currentContentHash) {
        String storedHash = existing.getContentHash();
        if (storedHash == null || storedHash.isBlank()) {
            storedHash = existing.getAcademicSnapshotJson() == null ? "" : contentHash(existing.getAcademicSnapshotJson());
        }
        return storedHash.equals(currentContentHash);
    }
    private String courseSnapshot(AcademicWorkload w) {
        var teacher = w.getTeacher();
        var person = teacher.getPerson();
        var course = w.getCourse();
        var period = w.getAcademicPeriod();
        return jsonObject(
                "certificateType", "COURSE",
                "teacherCode", teacher.getCode(),
                "teacherFullName", person == null ? "" : person.getFullName(),
                "department", teacher.getDepartment() == null ? "" : teacher.getDepartment().name(),
                "academicPeriod", period == null ? "" : period.getSemesterCode(),
                "courseCode", course == null ? "" : course.getCode(),
                "courseName", course == null ? "" : course.getName(),
                "cycle", Objects.toString(w.getCycle(), ""),
                "section", Objects.toString(w.getSection(), ""),
                "school", w.getSchool() == null ? "" : w.getSchool().name(),
                "plan", Objects.toString(w.getPlan(), ""),
                "workloadId", Objects.toString(w.getId(), ""));
    }
    private String semesterSnapshot(List<Certification> sourceRows) {
        var first = sourceRows.get(0);
        var teacher = first.getTeacher();
        var period = first.getAcademicPeriod();
        String courses = sourceRows.stream()
                .sorted(Comparator.comparing(c -> c.getAcademicWorkload().getId()))
                .map(c -> {
                    var w = c.getAcademicWorkload();
                    return jsonObject(
                            "academicWorkloadId", Objects.toString(w.getId(), ""),
                            "courseCode", w.getCourse().getCode(),
                            "courseName", w.getCourse().getName(),
                            "cycle", Objects.toString(w.getCycle(), ""),
                            "section", Objects.toString(w.getSection(), ""),
                            "school", w.getSchool().name(),
                            "plan", Objects.toString(w.getPlan(), ""),
                            "sourceCourseCertificationId", Objects.toString(c.getId(), ""),
                            "sourceCourseContentHash", Objects.toString(c.getContentHash(), ""),
                            "sourceCourseVersion", Objects.toString(versionOf(c), ""));
                })
                .reduce((left, right) -> left + "," + right).orElse("");
        return "{\"certificateType\":\"SEMESTER\",\"teacherCode\":\"" + escapeJson(teacher.getCode())
                + "\",\"teacherFullName\":\"" + escapeJson(teacher.getPerson().getFullName())
                + "\",\"department\":\"" + escapeJson(teacher.getDepartment() == null ? "" : teacher.getDepartment().name())
                + "\",\"academicPeriod\":\"" + escapeJson(period.getSemesterCode())
                + "\",\"courses\":[" + courses + "]}";
    }
    private String contentHash(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            var output = new StringBuilder(hash.length * 2);
            for (byte item : hash) output.append(String.format("%02x", item));
            return output.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
    private String teacherCode(Certification c) {
        return c.getTeacher() != null ? c.getTeacher().getCode() : c.getAcademicWorkload().getTeacher().getCode();
    }
    private List<Certification> latestVisibleCertificates(List<Certification> rows) {
        Map<String, Certification> latest = new LinkedHashMap<>();
        rows.stream().sorted(Comparator.comparing(Certification::getId)).forEach(c -> latest.put(logicalKey(c), c));
        return latest.values().stream().sorted(Comparator.comparing(Certification::getId).reversed()).toList();
    }
    private String logicalKey(Certification c) {
        if (c.getCertificateType() == CertificationType.SEMESTER) {
            return "semester-" + c.getTeacher().getId() + "-" + c.getAcademicPeriod().getId();
        }
        return "workload-" + c.getAcademicWorkload().getId();
    }
    private boolean isLatestVersion(Certification c) {
        var history = c.getCertificateType() == CertificationType.SEMESTER
                ? certificates.findByTeacherIdAndAcademicPeriodIdAndCertificateTypeOrderById(
                        c.getTeacher().getId(), c.getAcademicPeriod().getId(), CertificationType.SEMESTER)
                : certificates.findByAcademicWorkloadIdOrderById(c.getAcademicWorkload().getId());
        return !history.isEmpty() && history.getLast().getId().equals(c.getId());
    }
    private int nextVersion(List<Certification> history) {
        return history.stream().map(Certification::getVersionNumber).filter(Objects::nonNull).max(Integer::compareTo)
                .orElse(history.size()) + 1;
    }
    private int versionOf(Certification certification) {
        if (certification.getVersionNumber() != null) return certification.getVersionNumber();
        var history = certificates.findByAcademicWorkloadIdOrderById(certification.getAcademicWorkload().getId());
        for (int i = 0; i < history.size(); i++) if (history.get(i).getId().equals(certification.getId())) return i + 1;
        return 1;
    }
    private int computedVersion(Certification certification) {
        var history = historyRows(certification);
        for (int i = 0; i < history.size(); i++) if (history.get(i).getId().equals(certification.getId())) return i + 1;
        return 1;
    }
    private boolean isGenerated(Certification c) { return c.getStatus() != null && c.getStatus().isGenerated(); }
    private boolean isSigned(Certification c) { return c.getStatus() != null && c.getStatus().isSigned(); }
    private Instant generatedInstant(Certification c) {
        var generatedAt = c.getGeneratedAt() != null ? c.getGeneratedAt() : c.getCreatedAt();
        return generatedAt == null ? null : generatedAt.atZone(LIMA).toInstant();
    }
    private Instant signedInstant(Certification c) {
        return c.getSignedAt() == null ? null : c.getSignedAt().atZone(LIMA).toInstant();
    }
    private String jsonObject(String... keyValues) {
        var builder = new StringBuilder("{");
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i > 0) builder.append(',');
            builder.append('"').append(escapeJson(keyValues[i])).append("\":\"")
                    .append(escapeJson(keyValues[i + 1])).append('"');
        }
        return builder.append('}').toString();
    }
    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    private String semesterKey(String teacherCode, String semester) { return "semester-" + teacherCode + "-" + semester; }
    private String status(Certification c) { return c.getStatus() == null ? "GENERADA" : c.getStatus().official().name(); }
    private boolean canReadTeacher(Authentication auth, String teacherCode) {
        try {
            identity.requireTeacherAccess(auth, teacherCode);
            return true;
        } catch (ResponseStatusException ex) {
            return false;
        }
    }

    private boolean matches(Certification c, String certificateType, String targetStatus, String semester, String course) {
        if (certificateType != null && !certificateType.isBlank()
                && certificateType(certificateType) != c.getCertificateType()) {
            return false;
        }
        if (targetStatus != null && !targetStatus.isBlank()
                && !status(c).equalsIgnoreCase(targetStatus.trim())) {
            return false;
        }
        var period = c.getAcademicPeriod() != null ? c.getAcademicPeriod()
                : c.getAcademicWorkload() == null ? null : c.getAcademicWorkload().getAcademicPeriod();
        if (semester != null && !semester.isBlank()
                && (period == null || !period.getSemesterCode().equalsIgnoreCase(semester.trim()))) {
            return false;
        }
        if (course != null && !course.isBlank()) {
            var workload = c.getAcademicWorkload();
            if (workload == null || workload.getCourse() == null) {
                return false;
            }
            String needle = course.trim().toLowerCase(Locale.ROOT);
            return workload.getCourse().getCode().toLowerCase(Locale.ROOT).contains(needle)
                    || workload.getCourse().getName().toLowerCase(Locale.ROOT).contains(needle);
        }
        return true;
    }

    private CertificationType certificateType(String value) {
        try {
            return CertificationType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "certificateType debe ser COURSE o SEMESTER");
        }
    }

    private void requireCompleteSemesterSource(String teacherCode, String semester) {
        var expected = workloads.findByTeacherCodeAndAcademicPeriodSemesterCodeOrderById(teacherCode, semester);
        if (expected.isEmpty()) {
            return;
        }
        var sourceRows = latestCourseCertificatesByWorkload(certificates
                .findByTeacherCodeAndAcademicPeriodSemesterCodeAndCertificateTypeOrderById(
                        teacherCode, semester, CertificationType.COURSE)
                .stream()
                .filter(c -> isGenerated(c) || isSigned(c))
                .toList());
        var generatedWorkloads = sourceRows.stream()
                .map(Certification::getAcademicWorkload)
                .filter(Objects::nonNull)
                .map(AcademicWorkload::getId)
                .collect(java.util.stream.Collectors.toSet());
        var missing = expected.stream()
                .filter(workload -> !generatedWorkloads.contains(workload.getId()))
                .map(workload -> workload.getCourse().getCode() + "-" + workload.getSection())
                .toList();
        if (!missing.isEmpty()) {
            throw new IncompleteSemesterSourceException(expected.size(), sourceRows.size(), missing);
        }
    }

    private List<Certification> historyRows(Certification c) {
        if (c.getCertificateType() == CertificationType.SEMESTER) {
            return certificates.findByTeacherIdAndAcademicPeriodIdAndCertificateTypeOrderById(
                    c.getTeacher().getId(), c.getAcademicPeriod().getId(), CertificationType.SEMESTER);
        }
        return certificates.findByAcademicWorkloadIdOrderById(c.getAcademicWorkload().getId());
    }

    private int normalizePage(Integer page) {
        if (page == null) return DEFAULT_PAGE;
        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be greater than or equal to 0");
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) return DEFAULT_SIZE;
        if (size < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be greater than or equal to 1");
        if (size > MAX_SIZE) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be less than or equal to " + MAX_SIZE);
        return size;
    }

    public record CertificatePage(List<CertificateResponse> data, PaginatedResponse.Pagination pagination) {
    }
}
