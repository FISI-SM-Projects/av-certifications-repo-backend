package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.api;

public record CreateCertificateRequest(
        String certificateType,
        Long academicWorkloadId,
        String teacherCode,
        String semester,
        Boolean confirmIncomplete
) {
}
