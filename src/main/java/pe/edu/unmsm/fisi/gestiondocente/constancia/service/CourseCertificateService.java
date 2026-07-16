package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CourseCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.ApprovedCertificateAlreadyExistsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.TeacherIdentityMismatchException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.TeacherNotFoundForCertificateException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.pdf.PdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.CertificateGenerationRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CertificateRequestNormalizer;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CourseCertificateRequestNormalizer;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CourseCertificateRequestValidator;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Docente;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.DocenteRepository;

@Service
public class CourseCertificateService {

    private static final String REQUEST_FILE = "request.json";
    private static final String PDF_FILE = "certificate.pdf";

    private final CourseCertificateRequestValidator validator;
    private final CourseCertificateRequestNormalizer normalizer;
    private final CertificateIdService certificateIdService;
    private final CertificateGenerationRepository constanciaRepository;
    private final PdfGenerationService pdfGenerationService;
    private final DocenteRepository docenteRepository;
    private final CertificateKeyLockService lockService;
    private final Clock clock;

    @Autowired
    public CourseCertificateService(CourseCertificateRequestValidator validator,
            CourseCertificateRequestNormalizer normalizer,
            CertificateIdService certificateIdService,
            CertificateGenerationRepository constanciaRepository,
            PdfGenerationService pdfGenerationService,
            DocenteRepository docenteRepository,
            CertificateKeyLockService lockService) {
        this(validator, normalizer, certificateIdService, constanciaRepository, pdfGenerationService, docenteRepository,
                lockService, Clock.systemDefaultZone());
    }

    public CourseCertificateService(CourseCertificateRequestValidator validator,
            CertificateIdService certificateIdService,
            CertificateGenerationRepository constanciaRepository,
            PdfGenerationService pdfGenerationService,
            Clock clock) {
        this(validator, new CourseCertificateRequestNormalizer(), certificateIdService, constanciaRepository,
                pdfGenerationService, new DocenteRepository(), new CertificateKeyLockService(), clock);
    }

    public CourseCertificateService(CourseCertificateRequestValidator validator,
            CourseCertificateRequestNormalizer normalizer,
            CertificateIdService certificateIdService,
            CertificateGenerationRepository constanciaRepository,
            PdfGenerationService pdfGenerationService,
            DocenteRepository docenteRepository,
            Clock clock) {
        this(validator, normalizer, certificateIdService, constanciaRepository, pdfGenerationService, docenteRepository,
                new CertificateKeyLockService(), clock);
    }

    public CourseCertificateService(CourseCertificateRequestValidator validator,
            CourseCertificateRequestNormalizer normalizer,
            CertificateIdService certificateIdService,
            CertificateGenerationRepository constanciaRepository,
            PdfGenerationService pdfGenerationService,
            DocenteRepository docenteRepository,
            CertificateKeyLockService lockService,
            Clock clock) {
        this.validator = validator;
        this.normalizer = normalizer;
        this.certificateIdService = certificateIdService;
        this.constanciaRepository = constanciaRepository;
        this.pdfGenerationService = pdfGenerationService;
        this.docenteRepository = docenteRepository;
        this.lockService = lockService;
        this.clock = clock;
    }

    public CourseCertificateResponse generateCourseCertificate(CourseCertificateRequest request) {
        CourseCertificateRequest normalizedRequest = normalizer.normalize(request);
        validator.validate(normalizedRequest);
        CourseCertificateRequest authoritativeRequest = withAuthoritativeTeacher(normalizedRequest);

        String certificateKey = certificateIdService.buildCourseCertificateKey(
                authoritativeRequest.getTeacher().getTeacherCode(),
                authoritativeRequest.getCourse().getCode(),
                authoritativeRequest.getCourse().getSection(),
                authoritativeRequest.getCourse().getSemester());

        return lockService.executeLocked(certificateKey, () -> {
            if (constanciaRepository.existsApprovedByCertificateKey(certificateKey)) {
                throw new ApprovedCertificateAlreadyExistsException();
            }

            int version = constanciaRepository.nextVersion(certificateKey);
            String generationId = certificateIdService.buildGenerationId(certificateKey, version);
            CertificateGenerationMetadata metadata = buildMetadata(authoritativeRequest, certificateKey, generationId,
                    version);

            byte[] pdfBytes = pdfGenerationService.generateCourseCertificate(authoritativeRequest, metadata);
            CertificateGenerationMetadata storedMetadata =
                    constanciaRepository.saveGeneration(authoritativeRequest, metadata, pdfBytes);

            return buildResponse(authoritativeRequest, storedMetadata);
        });
    }

    private CourseCertificateRequest withAuthoritativeTeacher(CourseCertificateRequest request) {
        String teacherCode = request.getTeacher().getTeacherCode();
        Docente docente = docenteRepository.findByCodigo(teacherCode)
                .orElseThrow(() -> new TeacherNotFoundForCertificateException(teacherCode));

        String authoritativeFullName = normalizeFullName(docente);
        String authoritativeEmail = CertificateRequestNormalizer.email(docente.getCorreoInstitucional());

        if (!sameName(request.getTeacher().getFullName(), authoritativeFullName)
                || !authoritativeEmail.equals(CertificateRequestNormalizer.email(request.getTeacher().getEmail()))) {
            throw new TeacherIdentityMismatchException(
                    "La identidad del docente no coincide con el codigo docente registrado");
        }

        return new CourseCertificateRequest(
                new pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.TeacherPayload(
                        authoritativeFullName,
                        authoritativeEmail,
                        CertificateRequestNormalizer.code(docente.getCodigo())),
                request.getCourse(),
                request.getIssuer());
    }

    private String normalizeFullName(Docente docente) {
        return CertificateRequestNormalizer.normalizedNameForComparison(
                docente.getNombres() + " " + docente.getApellidos());
    }

    private boolean sameName(String providedName, String authoritativeName) {
        return CertificateRequestNormalizer.normalizedNameForComparison(providedName)
                .equals(CertificateRequestNormalizer.normalizedNameForComparison(authoritativeName));
    }

    private CertificateGenerationMetadata buildMetadata(CourseCertificateRequest request, String certificateKey,
            String generationId, int version) {
        return new CertificateGenerationMetadata(
                generationId,
                certificateKey,
                version,
                TipoConstancia.CURSO,
                EstadoConstancia.GENERADO,
                request.getTeacher().getTeacherCode(),
                request.getCourse().getCode(),
                request.getCourse().getSection(),
                request.getCourse().getSemester(),
                LocalDateTime.now(clock),
                REQUEST_FILE,
                PDF_FILE);
    }

    private CourseCertificateResponse buildResponse(CourseCertificateRequest request,
            CertificateGenerationMetadata metadata) {
        String viewUrl = "/api/v1/constancias/generaciones/" + metadata.getGenerationId() + "/pdf";
        String downloadUrl = "/api/v1/constancias/generaciones/" + metadata.getGenerationId() + "/download";

        return new CourseCertificateResponse(
                metadata.getGenerationId(),
                metadata.getCertificateKey(),
                metadata.getVersion(),
                metadata.getType(),
                metadata.getStatus(),
                request.getTeacher().getFullName(),
                request.getCourse().getCode(),
                request.getCourse().getSubject(),
                request.getCourse().getSection(),
                request.getCourse().getSemester(),
                metadata.getGeneratedAt(),
                viewUrl,
                downloadUrl);
    }
}
