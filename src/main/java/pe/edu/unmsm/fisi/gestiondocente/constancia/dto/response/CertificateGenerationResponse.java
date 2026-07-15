package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response;

import java.time.LocalDateTime;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;

public class CertificateGenerationResponse {

    private String generationId;
    private String certificateKey;
    private int version;
    private TipoConstancia type;
    private EstadoConstancia status;
    private String teacherCode;
    private String courseCode;
    private String section;
    private String semester;
    private LocalDateTime generatedAt;
    private String viewUrl;
    private String downloadUrl;

    public CertificateGenerationResponse() {
    }

    public CertificateGenerationResponse(String generationId, String certificateKey, int version,
            TipoConstancia type, EstadoConstancia status, String teacherCode, String courseCode, String section,
            String semester, LocalDateTime generatedAt, String viewUrl, String downloadUrl) {
        this.generationId = generationId;
        this.certificateKey = certificateKey;
        this.version = version;
        this.type = type;
        this.status = status;
        this.teacherCode = teacherCode;
        this.courseCode = courseCode;
        this.section = section;
        this.semester = semester;
        this.generatedAt = generatedAt;
        this.viewUrl = viewUrl;
        this.downloadUrl = downloadUrl;
    }

    public String getGenerationId() {
        return generationId;
    }

    public void setGenerationId(String generationId) {
        this.generationId = generationId;
    }

    public String getCertificateKey() {
        return certificateKey;
    }

    public void setCertificateKey(String certificateKey) {
        this.certificateKey = certificateKey;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public TipoConstancia getType() {
        return type;
    }

    public void setType(TipoConstancia type) {
        this.type = type;
    }

    public EstadoConstancia getStatus() {
        return status;
    }

    public void setStatus(EstadoConstancia status) {
        this.status = status;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
