package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response;

import java.time.LocalDateTime;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;

public class CourseCertificateResponse {

    private String generationId;
    private String certificateKey;
    private int version;
    private TipoConstancia type;
    private EstadoConstancia status;
    private String teacherFullName;
    private String courseCode;
    private String courseSubject;
    private String section;
    private String semester;
    private LocalDateTime generatedAt;
    private String viewUrl;
    private String downloadUrl;

    public CourseCertificateResponse() {
    }

    public CourseCertificateResponse(String generationId, String certificateKey, int version,
            TipoConstancia type, EstadoConstancia status, String teacherFullName, String courseCode,
            String courseSubject, String section, String semester, LocalDateTime generatedAt, String viewUrl,
            String downloadUrl) {
        this.generationId = generationId;
        this.certificateKey = certificateKey;
        this.version = version;
        this.type = type;
        this.status = status;
        this.teacherFullName = teacherFullName;
        this.courseCode = courseCode;
        this.courseSubject = courseSubject;
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

    public String getTeacherFullName() {
        return teacherFullName;
    }

    public void setTeacherFullName(String teacherFullName) {
        this.teacherFullName = teacherFullName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseSubject() {
        return courseSubject;
    }

    public void setCourseSubject(String courseSubject) {
        this.courseSubject = courseSubject;
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
