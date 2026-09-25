package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CertificateGenerationResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.CertificateGenerationNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.CertificatePdfNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.CertificateGenerationRepository;

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

    public List<CertificateGenerationResponse> listAuthenticatedTeacherCertificates(String teacherCode) {
        return constanciaRepository.findLatestByTeacherCode(teacherCode).stream()
                .map(metadata -> toResponse(metadata, true))
                .toList();
    }

    public CertificateGenerationResponse findAuthenticatedTeacherGeneration(String generationId, String teacherCode) {
        return toResponse(findOwnedGeneration(generationId, teacherCode), true);
    }

    public List<CertificateGenerationResponse> findAuthenticatedTeacherHistory(String certificateKey,
            String teacherCode) {
        return constanciaRepository.findHistoryByCertificateKey(certificateKey).stream()
                .filter(metadata -> teacherCode.equals(metadata.getTeacherCode()))
                .map(metadata -> toResponse(metadata, true))
                .toList();
    }

    public byte[] readAuthenticatedTeacherPdf(String generationId, String teacherCode) {
        findOwnedGeneration(generationId, teacherCode);
        return constanciaRepository.readPdf(generationId)
                .orElseThrow(() -> new CertificatePdfNotFoundException(generationId));
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
        return toResponse(metadata, false);
    }

    private CertificateGenerationMetadata findOwnedGeneration(String generationId, String teacherCode) {
        return constanciaRepository.findByGenerationId(generationId)
                .filter(metadata -> teacherCode.equals(metadata.getTeacherCode()))
                .orElseThrow(() -> new CertificateGenerationNotFoundException(generationId));
    }

    private CertificateGenerationResponse toResponse(CertificateGenerationMetadata metadata, boolean selfService) {
        String baseUrl = selfService ? "/api/v1/teachers/me/constancias" : "/api/v1/constancias";
        String viewUrl = baseUrl + "/generaciones/" + metadata.getGenerationId() + "/pdf";
        String downloadUrl = baseUrl + "/generaciones/" + metadata.getGenerationId() + "/download";

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
