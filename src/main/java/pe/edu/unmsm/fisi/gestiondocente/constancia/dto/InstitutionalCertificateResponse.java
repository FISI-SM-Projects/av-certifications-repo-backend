package pe.edu.unmsm.fisi.gestiondocente.constancia.dto;

import java.time.Instant;
public record InstitutionalCertificateResponse(String generationId, String certificateKey, int version,
        String type, String status, String teacherCode, String courseCode, String section, String semester,
        Instant generatedAt, String viewUrl, String downloadUrl, String courseSubject, String teacherFullName,
        boolean pdfAvailable) {}
