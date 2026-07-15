package pe.edu.unmsm.fisi.gestiondocente.constancia.entity;

import java.time.LocalDateTime;

public class CertificateGenerationMetadata {

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
    private String requestFile;
    private String pdfFile;

    public CertificateGenerationMetadata() {
    }

    public CertificateGenerationMetadata(String generationId, String certificateKey, int version,
            TipoConstancia type, EstadoConstancia status, String teacherCode, String courseCode, String section,
            String semester, LocalDateTime generatedAt, String requestFile, String pdfFile) {
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
        this.requestFile = requestFile;
        this.pdfFile = pdfFile;
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

    public String getRequestFile() {
        return requestFile;
    }

    public void setRequestFile(String requestFile) {
        this.requestFile = requestFile;
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }
}
