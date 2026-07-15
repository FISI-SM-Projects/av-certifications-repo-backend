package pe.edu.unmsm.fisi.gestiondocente.constancia.repository;

import java.util.List;
import java.util.Optional;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.Constancia;

public interface ConstanciaRepository {

    default List<Constancia> findByDocenteId(Long docenteId) {
        throw new UnsupportedOperationException("Operacion no soportada por este repositorio");
    }

    default CertificateGenerationMetadata saveGeneration(Object request,
            CertificateGenerationMetadata metadata, byte[] pdfBytes) {
        throw new UnsupportedOperationException("Operacion no soportada por este repositorio");
    }

    default <T> Optional<T> readRequest(String generationId, Class<T> requestType) {
        throw new UnsupportedOperationException("Operacion no soportada por este repositorio");
    }

    default Optional<CertificateGenerationMetadata> findByGenerationId(String generationId) {
        throw new UnsupportedOperationException("Operacion no soportada por este repositorio");
    }

    default List<CertificateGenerationMetadata> findHistoryByCertificateKey(String certificateKey) {
        throw new UnsupportedOperationException("Operacion no soportada por este repositorio");
    }

    default Optional<CertificateGenerationMetadata> findLatestByCertificateKey(String certificateKey) {
        throw new UnsupportedOperationException("Operacion no soportada por este repositorio");
    }

    default List<CertificateGenerationMetadata> findLatestByTeacherCode(String teacherCode) {
        throw new UnsupportedOperationException("Operacion no soportada por este repositorio");
    }

    default List<CertificateGenerationMetadata> findByTeacherCodeAndSemester(String teacherCode, String semester) {
        throw new UnsupportedOperationException("Operacion no soportada por este repositorio");
    }

    default int nextVersion(String certificateKey) {
        throw new UnsupportedOperationException("Operacion no soportada por este repositorio");
    }

    default boolean existsApprovedByCertificateKey(String certificateKey) {
        throw new UnsupportedOperationException("Operacion no soportada por este repositorio");
    }

    default Optional<byte[]> readPdf(String generationId) {
        throw new UnsupportedOperationException("Operacion no soportada por este repositorio");
    }
}
