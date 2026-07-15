package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.StoragePathSanitizer;

@Service
public class CertificateIdService {

    private final StoragePathSanitizer storagePathSanitizer;

    public CertificateIdService() {
        this(new StoragePathSanitizer());
    }

    @Autowired
    public CertificateIdService(StoragePathSanitizer storagePathSanitizer) {
        this.storagePathSanitizer = storagePathSanitizer;
    }

    public String buildCourseCertificateKey(String teacherCode, String courseCode, String section, String semester) {
        return String.join("-",
                storagePathSanitizer.sanitizeSegment(teacherCode),
                storagePathSanitizer.sanitizeSegment(courseCode),
                storagePathSanitizer.sanitizeSegment(section),
                storagePathSanitizer.sanitizeSegment(semester));
    }

    public String buildSemesterCertificateKey(String teacherCode, String semester) {
        return String.join("-",
                storagePathSanitizer.sanitizeSegment(teacherCode),
                storagePathSanitizer.sanitizeSegment(semester));
    }

    public String buildGenerationId(String certificateKey, int version) {
        if (version < 1) {
            throw new IllegalArgumentException("La version debe ser mayor o igual a 1");
        }

        return storagePathSanitizer.sanitizeSegment(certificateKey) + "-v" + String.format("%03d", version);
    }
}
