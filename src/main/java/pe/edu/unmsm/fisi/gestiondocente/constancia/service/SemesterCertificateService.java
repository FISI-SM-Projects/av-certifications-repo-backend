package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.SemesterCertificateSource;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.SemesterCertificateSourceSummary;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.ExpectedCourseRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.SemesterCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.SemesterCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.ApprovedCertificateAlreadyExistsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.CertificateSourceInconsistencyException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingExpectedCoursesException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.pdf.PdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.ConstanciaRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.SemesterCertificateRequestValidator;

@Service
public class SemesterCertificateService {

    private static final String SOURCE_SUMMARY_FILE = "source-summary.json";
    private static final String PDF_FILE = "certificate.pdf";

    private final SemesterCertificateRequestValidator validator;
    private final CertificateIdService certificateIdService;
    private final ConstanciaRepository constanciaRepository;
    private final PdfGenerationService pdfGenerationService;
    private final Clock clock;

    @Autowired
    public SemesterCertificateService(SemesterCertificateRequestValidator validator,
            CertificateIdService certificateIdService,
            @Qualifier("fileSystemConstanciaRepository") ConstanciaRepository constanciaRepository,
            PdfGenerationService pdfGenerationService) {
        this(validator, certificateIdService, constanciaRepository, pdfGenerationService, Clock.systemDefaultZone());
    }

    public SemesterCertificateService(SemesterCertificateRequestValidator validator,
            CertificateIdService certificateIdService,
            ConstanciaRepository constanciaRepository,
            PdfGenerationService pdfGenerationService,
            Clock clock) {
        this.validator = validator;
        this.certificateIdService = certificateIdService;
        this.constanciaRepository = constanciaRepository;
        this.pdfGenerationService = pdfGenerationService;
        this.clock = clock;
    }

    public SemesterCertificateResponse generateSemesterCertificate(SemesterCertificateRequest request) {
        validator.validate(request);

        String certificateKey = certificateIdService.buildSemesterCertificateKey(
                request.getTeacherCode(),
                request.getSemester());

        if (constanciaRepository.existsApprovedByCertificateKey(certificateKey)) {
            throw new ApprovedCertificateAlreadyExistsException();
        }

        SourceSelectionResult sourceSelection = selectSources(request);
        if (!sourceSelection.missingCourses().isEmpty()) {
            throw new MissingExpectedCoursesException(sourceSelection.missingCourses());
        }

        SemesterCertificateSourceSummary sourceSummary = buildSourceSummary(request, sourceSelection.sources());
        int version = constanciaRepository.nextVersion(certificateKey);
        String generationId = certificateIdService.buildGenerationId(certificateKey, version);
        CertificateGenerationMetadata metadata = buildMetadata(request, certificateKey, generationId, version);

        byte[] pdfBytes = pdfGenerationService.generateSemesterCertificate(sourceSummary, metadata);
        CertificateGenerationMetadata storedMetadata =
                constanciaRepository.saveGeneration(sourceSummary, metadata, pdfBytes);

        return buildResponse(sourceSummary, storedMetadata);
    }

    private SourceSelectionResult selectSources(SemesterCertificateRequest request) {
        List<SemesterCertificateSource> sources = new ArrayList<>();
        List<ExpectedCourseRequest> missingCourses = new ArrayList<>();

        for (ExpectedCourseRequest expectedCourse : request.getExpectedCourses()) {
            String courseCertificateKey = certificateIdService.buildCourseCertificateKey(
                    request.getTeacherCode(),
                    expectedCourse.getCode(),
                    expectedCourse.getSection(),
                    request.getSemester());

            CertificateGenerationMetadata metadata = constanciaRepository.findLatestByCertificateKey(
                    courseCertificateKey)
                    .filter(this::isCourseCertificate)
                    .filter(source -> request.getTeacherCode().equals(source.getTeacherCode()))
                    .filter(source -> request.getSemester().equals(source.getSemester()))
                    .filter(source -> expectedCourse.getCode().equals(source.getCourseCode()))
                    .filter(source -> expectedCourse.getSection().equals(source.getSection()))
                    .orElse(null);

            if (metadata == null) {
                missingCourses.add(new ExpectedCourseRequest(expectedCourse.getCode(), expectedCourse.getSection()));
            } else {
                sources.add(buildSource(metadata, request));
            }
        }

        return new SourceSelectionResult(sources, missingCourses);
    }

    private boolean isCourseCertificate(CertificateGenerationMetadata metadata) {
        return metadata.getType() == TipoConstancia.CURSO;
    }

    private SemesterCertificateSource buildSource(CertificateGenerationMetadata metadata,
            SemesterCertificateRequest semesterRequest) {
        CourseCertificateRequest courseRequest = constanciaRepository
                .readRequest(metadata.getGenerationId(), CourseCertificateRequest.class)
                .orElseThrow(() -> new CertificateSourceInconsistencyException(
                        "No se pudo leer la solicitud fuente de la constancia por curso"));

        validateSourceConsistency(metadata, courseRequest, semesterRequest);

        return new SemesterCertificateSource(
                metadata.getGenerationId(),
                metadata.getCertificateKey(),
                courseRequest.getCourse().getCode(),
                courseRequest.getCourse().getSubject(),
                courseRequest.getCourse().getSection(),
                courseRequest.getCourse().getSchool(),
                courseRequest.getCourse().getPlan(),
                metadata.getStatus());
    }

    private void validateSourceConsistency(CertificateGenerationMetadata metadata,
            CourseCertificateRequest courseRequest,
            SemesterCertificateRequest semesterRequest) {
        if (courseRequest == null || courseRequest.getTeacher() == null || courseRequest.getCourse() == null) {
            throw new CertificateSourceInconsistencyException("La solicitud fuente esta incompleta");
        }
        if (!semesterRequest.getTeacherCode().equals(courseRequest.getTeacher().getTeacherCode())
                || !semesterRequest.getTeacherCode().equals(metadata.getTeacherCode())) {
            throw new CertificateSourceInconsistencyException("La fuente pertenece a otro docente");
        }
        if (!semesterRequest.getSemester().equals(courseRequest.getCourse().getSemester())
                || !semesterRequest.getSemester().equals(metadata.getSemester())) {
            throw new CertificateSourceInconsistencyException("La fuente pertenece a otro semestre");
        }
    }

    private SemesterCertificateSourceSummary buildSourceSummary(SemesterCertificateRequest request,
            List<SemesterCertificateSource> sources) {
        CourseCertificateRequest firstSourceRequest = constanciaRepository
                .readRequest(sources.get(0).getGenerationId(), CourseCertificateRequest.class)
                .orElseThrow(() -> new CertificateSourceInconsistencyException(
                        "No se pudo leer la identidad del docente fuente"));

        return new SemesterCertificateSourceSummary(
                request.getTeacherCode(),
                firstSourceRequest.getTeacher().getFullName(),
                firstSourceRequest.getTeacher().getEmail(),
                request.getSemester(),
                sources);
    }

    private CertificateGenerationMetadata buildMetadata(SemesterCertificateRequest request, String certificateKey,
            String generationId, int version) {
        return new CertificateGenerationMetadata(
                generationId,
                certificateKey,
                version,
                TipoConstancia.SEMESTRAL,
                EstadoConstancia.GENERADO,
                request.getTeacherCode(),
                null,
                null,
                request.getSemester(),
                LocalDateTime.now(clock),
                SOURCE_SUMMARY_FILE,
                PDF_FILE);
    }

    private SemesterCertificateResponse buildResponse(SemesterCertificateSourceSummary sourceSummary,
            CertificateGenerationMetadata metadata) {
        List<String> sourceGenerationIds = sourceSummary.getSourceGenerations().stream()
                .map(SemesterCertificateSource::getGenerationId)
                .toList();
        String viewUrl = "/api/v1/constancias/generaciones/" + metadata.getGenerationId() + "/pdf";
        String downloadUrl = "/api/v1/constancias/generaciones/" + metadata.getGenerationId() + "/download";

        return new SemesterCertificateResponse(
                metadata.getGenerationId(),
                metadata.getCertificateKey(),
                metadata.getVersion(),
                metadata.getType(),
                metadata.getStatus(),
                sourceSummary.getTeacherCode(),
                sourceSummary.getTeacherFullName(),
                sourceSummary.getSemester(),
                sourceSummary.getSourceGenerations().size(),
                sourceGenerationIds,
                metadata.getGeneratedAt(),
                viewUrl,
                downloadUrl);
    }

    private record SourceSelectionResult(
            List<SemesterCertificateSource> sources,
            List<ExpectedCourseRequest> missingCourses) {
    }
}
