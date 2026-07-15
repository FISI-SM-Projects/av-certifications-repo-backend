package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CourseCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.ApprovedCertificateAlreadyExistsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.pdf.PdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.ConstanciaRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CourseCertificateRequestValidator;

@Service
public class CourseCertificateService {

    private static final String REQUEST_FILE = "request.json";
    private static final String PDF_FILE = "certificate.pdf";

    private final CourseCertificateRequestValidator validator;
    private final CertificateIdService certificateIdService;
    private final ConstanciaRepository constanciaRepository;
    private final PdfGenerationService pdfGenerationService;
    private final Clock clock;

    @Autowired
    public CourseCertificateService(CourseCertificateRequestValidator validator,
            CertificateIdService certificateIdService,
            @Qualifier("fileSystemConstanciaRepository") ConstanciaRepository constanciaRepository,
            PdfGenerationService pdfGenerationService) {
        this(validator, certificateIdService, constanciaRepository, pdfGenerationService, Clock.systemDefaultZone());
    }

    public CourseCertificateService(CourseCertificateRequestValidator validator,
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

    public CourseCertificateResponse generateCourseCertificate(CourseCertificateRequest request) {
        validator.validate(request);

        String certificateKey = certificateIdService.buildCourseCertificateKey(
                request.getTeacher().getTeacherCode(),
                request.getCourse().getCode(),
                request.getCourse().getSection(),
                request.getCourse().getSemester());

        if (constanciaRepository.existsApprovedByCertificateKey(certificateKey)) {
            throw new ApprovedCertificateAlreadyExistsException();
        }

        int version = constanciaRepository.nextVersion(certificateKey);
        String generationId = certificateIdService.buildGenerationId(certificateKey, version);
        CertificateGenerationMetadata metadata = buildMetadata(request, certificateKey, generationId, version);

        byte[] pdfBytes = pdfGenerationService.generateCourseCertificate(request, metadata);
        CertificateGenerationMetadata storedMetadata =
                constanciaRepository.saveGeneration(request, metadata, pdfBytes);

        return buildResponse(request, storedMetadata);
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
