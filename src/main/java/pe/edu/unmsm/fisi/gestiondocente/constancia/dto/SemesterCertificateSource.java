package pe.edu.unmsm.fisi.gestiondocente.constancia.dto;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;

public class SemesterCertificateSource {

    private String generationId;
    private String certificateKey;
    private String courseCode;
    private String courseSubject;
    private String section;
    private String school;
    private String plan;
    private EstadoConstancia status;

    public SemesterCertificateSource() {
    }

    public SemesterCertificateSource(String generationId, String certificateKey, String courseCode,
            String courseSubject, String section, String school, String plan, EstadoConstancia status) {
        this.generationId = generationId;
        this.certificateKey = certificateKey;
        this.courseCode = courseCode;
        this.courseSubject = courseSubject;
        this.section = section;
        this.school = school;
        this.plan = plan;
        this.status = status;
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

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public EstadoConstancia getStatus() {
        return status;
    }

    public void setStatus(EstadoConstancia status) {
        this.status = status;
    }
}
