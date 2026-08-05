package pe.edu.unmsm.fisi.gestiondocente.service.pdf;

import pe.edu.unmsm.fisi.gestiondocente.dto.constancia.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.dto.constancia.SemesterCertificateSourceSummary;
import pe.edu.unmsm.fisi.gestiondocente.entity.constancia.CertificateGenerationMetadata;

public interface PdfGenerationService {

    byte[] generateCourseCertificate(CourseCertificateRequest request, CertificateGenerationMetadata metadata);

    byte[] generateSemesterCertificate(SemesterCertificateSourceSummary sourceSummary,
            CertificateGenerationMetadata metadata);
}
