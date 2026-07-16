package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response;

import java.time.Instant;
import java.util.List;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;

public class SemesterCertificateResponse {

    private String generationId;
    private String certificateKey;
    private int version;
    private TipoConstancia type;
    private EstadoConstancia status;
    private String teacherCode;
    private String teacherFullName;
    private String semester;
    private int courseCount;
    private List<String> sourceGenerationIds;
    private Instant generatedAt;
    private String viewUrl;
    private String downloadUrl;

    public SemesterCertificateResponse() {
    }

    public SemesterCertificateResponse(String generationId, String certificateKey, int version,
            TipoConstancia type, EstadoConstancia status, String teacherCode, String teacherFullName,
            String semester, int courseCount, List<String> sourceGenerationIds, Instant generatedAt,
            String viewUrl, String downloadUrl) {
        this.generationId = generationId;
        this.certificateKey = certificateKey;
        this.version = version;
        this.type = type;
        this.status = status;
        this.teacherCode = teacherCode;
        this.teacherFullName = teacherFullName;
        this.semester = semester;
        this.courseCount = courseCount;
        this.sourceGenerationIds = List.copyOf(sourceGenerationIds);
        this.generatedAt = generatedAt;
        this.viewUrl = viewUrl;
        this.downloadUrl = downloadUrl;
    }

    public String getGenerationId() {
        return generationId;
    }

    public String getCertificateKey() {
        return certificateKey;
    }

    public int getVersion() {
        return version;
    }

    public TipoConstancia getType() {
        return type;
    }

    public EstadoConstancia getStatus() {
        return status;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public String getTeacherFullName() {
        return teacherFullName;
    }

    public String getSemester() {
        return semester;
    }

    public int getCourseCount() {
        return courseCount;
    }

    public List<String> getSourceGenerationIds() {
        return sourceGenerationIds;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
