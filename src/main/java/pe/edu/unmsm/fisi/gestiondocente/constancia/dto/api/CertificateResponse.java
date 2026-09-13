package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.api;

import java.time.Instant;

public record CertificateResponse(
        Long id,
        String certificateKey,
        String certificateType,
        String status,
        Integer version,
        Long teacherId,
        String teacherCode,
        String teacherFullName,
        Long academicPeriodId,
        String semester,
        Long academicWorkloadId,
        CourseSummary course,
        Integer section,
        Integer cycle,
        String school,
        Integer plan,
        Instant generatedAt,
        Instant signedAt,
        Boolean pdfAvailable,
        String documentUrl
) {
    public record CourseSummary(Long id, String code, String name) {
    }
}
