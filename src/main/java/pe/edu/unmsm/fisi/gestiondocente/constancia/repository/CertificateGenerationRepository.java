package pe.edu.unmsm.fisi.gestiondocente.constancia.repository;

import java.util.List;
import java.util.Optional;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;

public interface CertificateGenerationRepository {

    CertificateGenerationMetadata saveGeneration(Object request,
            CertificateGenerationMetadata metadata, byte[] pdfBytes);

    <T> Optional<T> readRequest(String generationId, Class<T> requestType);

    Optional<CertificateGenerationMetadata> findByGenerationId(String generationId);

    List<CertificateGenerationMetadata> findHistoryByCertificateKey(String certificateKey);

    Optional<CertificateGenerationMetadata> findLatestByCertificateKey(String certificateKey);

    List<CertificateGenerationMetadata> findLatestByTeacherCode(String teacherCode);

    List<CertificateGenerationMetadata> findByTeacherCodeAndSemester(String teacherCode, String semester);

    int nextVersion(String certificateKey);

    boolean existsApprovedByCertificateKey(String certificateKey);

    Optional<byte[]> readPdf(String generationId);
}
