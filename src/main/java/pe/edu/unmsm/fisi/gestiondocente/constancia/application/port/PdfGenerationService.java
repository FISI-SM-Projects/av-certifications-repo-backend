package pe.edu.unmsm.fisi.gestiondocente.constancia.application.port;

import pe.edu.unmsm.fisi.gestiondocente.constancia.web.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.web.dto.SemesterCertificateSourceSummary;
import pe.edu.unmsm.fisi.gestiondocente.constancia.domain.CertificateGenerationMetadata;

public interface PdfGenerationService {

    byte[] generateCourseCertificate(CourseCertificateRequest request, CertificateGenerationMetadata metadata);

    byte[] generateSemesterCertificate(SemesterCertificateSourceSummary sourceSummary,
            CertificateGenerationMetadata metadata);
}
