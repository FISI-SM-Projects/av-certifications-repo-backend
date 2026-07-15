package pe.edu.unmsm.fisi.gestiondocente.constancia.pdf;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.SemesterCertificateSourceSummary;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;

public interface PdfGenerationService {

    byte[] generateCourseCertificate(CourseCertificateRequest request, CertificateGenerationMetadata metadata);

    byte[] generateSemesterCertificate(SemesterCertificateSourceSummary sourceSummary,
            CertificateGenerationMetadata metadata);
}
