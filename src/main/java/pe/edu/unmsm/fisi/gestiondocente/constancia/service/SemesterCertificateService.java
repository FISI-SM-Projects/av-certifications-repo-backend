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
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.TeacherNotFoundForCertificateException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.pdf.PdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.ConstanciaRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CertificateRequestNormalizer;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CourseCertificateRequestNormalizer;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.SemesterCertificateRequestNormalizer;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.SemesterCertificateRequestValidator;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Docente;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.DocenteRepository;

@Service
public class SemesterCertificateService {

    private static final String SOURCE_SUMMARY_FILE = "source-summary.json";
    private static final String PDF_FILE = "certificate.pdf";

    private final SemesterCertificateRequestValidator validator;
    private final SemesterCertificateRequestNormalizer normalizer;
    private final CourseCertificateRequestNormalizer courseRequestNormalizer;
    private final CertificateIdService certificateIdService;
    private final ConstanciaRepository constanciaRepository;
    private final PdfGenerationService pdfGenerationService;
    private final DocenteRepository docenteRepository;
    private final CertificateKeyLockService lockService;
    private final Clock clock;

    @Autowired
    public SemesterCertificateService(SemesterCertificateRequestValidator validator,
            SemesterCertificateRequestNormalizer normalizer,
            CourseCertificateRequestNormalizer courseRequestNormalizer,
            CertificateIdService certificateIdService,
            @Qualifier("fileSystemConstanciaRepository") ConstanciaRepository constanciaRepository,
            PdfGenerationService pdfGenerationService,
            DocenteRepository docenteRepository,
            CertificateKeyLockService lockService) {
        this(validator, normalizer, courseRequestNormalizer, certificateIdService, constanciaRepository,
                pdfGenerationService, docenteRepository, lockService, Clock.systemDefaultZone());
    }

    public SemesterCertificateService(SemesterCertificateRequestValidator validator,
            CertificateIdService certificateIdService,
            ConstanciaRepository constanciaRepository,
            PdfGenerationService pdfGenerationService,
            Clock clock) {
        this(validator, new SemesterCertificateRequestNormalizer(), new CourseCertificateRequestNormalizer(),
                certificateIdService, constanciaRepository, pdfGenerationService, new DocenteRepository(),
                new CertificateKeyLockService(), clock);
    }

    public SemesterCertificateService(SemesterCertificateRequestValidator validator,
            SemesterCertificateRequestNormalizer normalizer,
            CourseCertificateRequestNormalizer courseRequestNormalizer,
            CertificateIdService certificateIdService,
            ConstanciaRepository constanciaRepository,
            PdfGenerationService pdfGenerationService,
            DocenteRepository docenteRepository,
            Clock clock) {
        this(validator, normalizer, courseRequestNormalizer, certificateIdService, constanciaRepository,
                pdfGenerationService, docenteRepository, new CertificateKeyLockService(), clock);
    }

    public SemesterCertificateService(SemesterCertificateRequestValidator validator,
            SemesterCertificateRequestNormalizer normalizer,
            CourseCertificateRequestNormalizer courseRequestNormalizer,
            CertificateIdService certificateIdService,
            ConstanciaRepository constanciaRepository,
            PdfGenerationService pdfGenerationService,
            DocenteRepository docenteRepository,
            CertificateKeyLockService lockService,
            Clock clock) {
        this.validator = validator;
        this.normalizer = normalizer;
        this.courseRequestNormalizer = courseRequestNormalizer;
        this.certificateIdService = certificateIdService;
        this.constanciaRepository = constanciaRepository;
        this.pdfGenerationService = pdfGenerationService;
        this.docenteRepository = docenteRepository;
        this.lockService = lockService;
        this.clock = clock;
    }

    public SemesterCertificateResponse generateSemesterCertificate(SemesterCertificateRequest request) {
        SemesterCertificateRequest normalizedRequest = normalizer.normalize(request);
        validator.validate(normalizedRequest);
        Docente docente = docenteRepository.findByCodigo(normalizedRequest.getTeacherCode())
                .orElseThrow(() -> new TeacherNotFoundForCertificateException(normalizedRequest.getTeacherCode()));

        String certificateKey = certificateIdService.buildSemesterCertificateKey(
                normalizedRequest.getTeacherCode(),
                normalizedRequest.getSemester());

        return lockService.executeLocked(certificateKey, () -> {
            if (constanciaRepository.existsApprovedByCertificateKey(certificateKey)) {
                throw new ApprovedCertificateAlreadyExistsException();
            }

            SourceSelectionResult sourceSelection = selectSources(normalizedRequest, docente);
            if (!sourceSelection.missingCourses().isEmpty()) {
                throw new MissingExpectedCoursesException(sourceSelection.missingCourses());
            }

            SemesterCertificateSourceSummary sourceSummary = buildSourceSummary(normalizedRequest,
                    sourceSelection.sources(), docente);
            int version = constanciaRepository.nextVersion(certificateKey);
            String generationId = certificateIdService.buildGenerationId(certificateKey, version);
            CertificateGenerationMetadata metadata = buildMetadata(normalizedRequest, certificateKey, generationId,
                    version);

            byte[] pdfBytes = pdfGenerationService.generateSemesterCertificate(sourceSummary, metadata);
            CertificateGenerationMetadata storedMetadata =
                    constanciaRepository.saveGeneration(sourceSummary, metadata, pdfBytes);

            return buildResponse(sourceSummary, storedMetadata);
        });
    }

    private SourceSelectionResult selectSources(SemesterCertificateRequest request, Docente docente) {
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
                sources.add(buildSource(metadata, request, expectedCourse, docente));
            }
        }

        return new SourceSelectionResult(sources, missingCourses);
    }

    private boolean isCourseCertificate(CertificateGenerationMetadata metadata) {
        return metadata.getType() == TipoConstancia.CURSO;
    }

    private SemesterCertificateSource buildSource(CertificateGenerationMetadata metadata,
            SemesterCertificateRequest semesterRequest,
            ExpectedCourseRequest expectedCourse,
            Docente docente) {
        CourseCertificateRequest courseRequest = constanciaRepository
                .readRequest(metadata.getGenerationId(), CourseCertificateRequest.class)
                .orElseThrow(() -> new CertificateSourceInconsistencyException(
                        "No se pudo leer la solicitud fuente de la constancia por curso"));
        CourseCertificateRequest normalizedCourseRequest = courseRequestNormalizer.normalize(courseRequest);

        validateSourceConsistency(metadata, normalizedCourseRequest, semesterRequest, expectedCourse, docente);

        return new SemesterCertificateSource(
                metadata.getGenerationId(),
                metadata.getCertificateKey(),
                normalizedCourseRequest.getCourse().getCode(),
                normalizedCourseRequest.getCourse().getSubject(),
                normalizedCourseRequest.getCourse().getSection(),
                normalizedCourseRequest.getCourse().getSchool(),
                normalizedCourseRequest.getCourse().getPlan(),
                metadata.getStatus());
    }

    private void validateSourceConsistency(CertificateGenerationMetadata metadata,
            CourseCertificateRequest courseRequest,
            SemesterCertificateRequest semesterRequest,
            ExpectedCourseRequest expectedCourse,
            Docente docente) {
        if (courseRequest == null || courseRequest.getTeacher() == null || courseRequest.getCourse() == null) {
            throw new CertificateSourceInconsistencyException("La solicitud fuente esta incompleta");
        }
        if (!isCourseCertificate(metadata)) {
            throw new CertificateSourceInconsistencyException("La fuente no corresponde a una constancia por curso");
        }
        if (!semesterRequest.getTeacherCode().equals(courseRequest.getTeacher().getTeacherCode())
                || !semesterRequest.getTeacherCode().equals(metadata.getTeacherCode())) {
            throw new CertificateSourceInconsistencyException("La fuente pertenece a otro docente");
        }
        if (!semesterRequest.getSemester().equals(courseRequest.getCourse().getSemester())
                || !semesterRequest.getSemester().equals(metadata.getSemester())) {
            throw new CertificateSourceInconsistencyException("La fuente pertenece a otro semestre");
        }
        if (!expectedCourse.getCode().equals(courseRequest.getCourse().getCode())
                || !expectedCourse.getCode().equals(metadata.getCourseCode())) {
            throw new CertificateSourceInconsistencyException(
                    "La fuente no coincide con el codigo de curso esperado: " + metadata.getGenerationId());
        }
        if (!expectedCourse.getSection().equals(courseRequest.getCourse().getSection())
                || !expectedCourse.getSection().equals(metadata.getSection())) {
            throw new CertificateSourceInconsistencyException(
                    "La fuente no coincide con la seccion esperada: " + metadata.getGenerationId());
        }

        String recalculatedKey = certificateIdService.buildCourseCertificateKey(
                courseRequest.getTeacher().getTeacherCode(),
                courseRequest.getCourse().getCode(),
                courseRequest.getCourse().getSection(),
                courseRequest.getCourse().getSemester());
        String recalculatedGenerationId = certificateIdService.buildGenerationId(recalculatedKey, metadata.getVersion());

        if (!recalculatedKey.equals(metadata.getCertificateKey())
                || !recalculatedGenerationId.equals(metadata.getGenerationId())) {
            throw new CertificateSourceInconsistencyException(
                    "La metadata de fuente no coincide con su solicitud: " + metadata.getGenerationId());
        }

        String authoritativeName = CertificateRequestNormalizer.normalizedNameForComparison(
                docente.getNombres() + " " + docente.getApellidos());
        String sourceName = CertificateRequestNormalizer.normalizedNameForComparison(
                courseRequest.getTeacher().getFullName());
        String authoritativeEmail = CertificateRequestNormalizer.email(docente.getCorreoInstitucional());
        String sourceEmail = CertificateRequestNormalizer.email(courseRequest.getTeacher().getEmail());

        if (!authoritativeName.equals(sourceName) || !authoritativeEmail.equals(sourceEmail)) {
            throw new CertificateSourceInconsistencyException(
                    "La identidad de la fuente no coincide con el docente registrado: " + metadata.getGenerationId());
        }
    }

    private SemesterCertificateSourceSummary buildSourceSummary(SemesterCertificateRequest request,
            List<SemesterCertificateSource> sources,
            Docente docente) {
        validateSources(sources);

        return new SemesterCertificateSourceSummary(
                request.getTeacherCode(),
                CertificateRequestNormalizer.normalizedNameForComparison(docente.getNombres() + " " + docente.getApellidos()),
                CertificateRequestNormalizer.email(docente.getCorreoInstitucional()),
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
        List<SemesterCertificateSource> sources = sourceSummary.getSourceGenerations();
        validateSources(sources);

        List<String> sourceGenerationIds = sources.stream()
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
                sources.size(),
                sourceGenerationIds,
                metadata.getGeneratedAt(),
                viewUrl,
                downloadUrl);
    }

    private void validateSources(List<SemesterCertificateSource> sources) {
        if (sources == null || sources.isEmpty()) {
            throw new CertificateSourceInconsistencyException("No existen fuentes para la constancia semestral");
        }

        for (SemesterCertificateSource source : sources) {
            if (source == null || isBlank(source.getGenerationId()) || isBlank(source.getCertificateKey())
                    || isBlank(source.getCourseCode()) || isBlank(source.getSection())
                    || source.getStatus() == null) {
                throw new CertificateSourceInconsistencyException("La fuente de constancia semestral esta incompleta");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record SourceSelectionResult(
            List<SemesterCertificateSource> sources,
            List<ExpectedCourseRequest> missingCourses) {
    }
}
