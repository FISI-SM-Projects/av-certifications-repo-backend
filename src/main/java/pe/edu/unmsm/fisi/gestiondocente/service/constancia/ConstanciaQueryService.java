package pe.edu.unmsm.fisi.gestiondocente.service.constancia;

import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.dto.constancia.response.CertificateGenerationResponse;
import pe.edu.unmsm.fisi.gestiondocente.entity.constancia.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.exception.constancia.CertificateGenerationNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.exception.constancia.CertificatePdfNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.repository.constancia.CertificateGenerationRepository;

@Service
public class ConstanciaQueryService {

    private final CertificateGenerationRepository constanciaRepository;

    public ConstanciaQueryService(CertificateGenerationRepository constanciaRepository) {
        this.constanciaRepository = constanciaRepository;
    }

    public List<CertificateGenerationResponse> listLatestByTeacherCode(String teacherCode) {
        return constanciaRepository.findLatestByTeacherCode(teacherCode).stream()
                .map(this::toResponse)
                .toList();
    }

    public CertificateGenerationResponse findByGenerationId(String generationId) {
        return constanciaRepository.findByGenerationId(generationId)
                .map(this::toResponse)
                .orElseThrow(() -> new CertificateGenerationNotFoundException(generationId));
    }

    public List<CertificateGenerationResponse> findHistoryByCertificateKey(String certificateKey) {
        return constanciaRepository.findHistoryByCertificateKey(certificateKey).stream()
                .map(this::toResponse)
                .toList();
    }

    public byte[] readPdf(String generationId) {
        ensureGenerationExists(generationId);
        return constanciaRepository.readPdf(generationId)
                .orElseThrow(() -> new CertificatePdfNotFoundException(generationId));
    }

    private void ensureGenerationExists(String generationId) {
        if (constanciaRepository.findByGenerationId(generationId).isEmpty()) {
            throw new CertificateGenerationNotFoundException(generationId);
        }
    }

    private CertificateGenerationResponse toResponse(CertificateGenerationMetadata metadata) {
        String viewUrl = "/api/v1/constancias/generaciones/" + metadata.getGenerationId() + "/pdf";
        String downloadUrl = "/api/v1/constancias/generaciones/" + metadata.getGenerationId() + "/download";

        return new CertificateGenerationResponse(
                metadata.getGenerationId(),
                metadata.getCertificateKey(),
                metadata.getVersion(),
                metadata.getType(),
                metadata.getStatus(),
                metadata.getTeacherCode(),
                metadata.getCourseCode(),
                metadata.getSection(),
                metadata.getSemester(),
                metadata.getGeneratedAt(),
                viewUrl,
                downloadUrl);
    }
}
