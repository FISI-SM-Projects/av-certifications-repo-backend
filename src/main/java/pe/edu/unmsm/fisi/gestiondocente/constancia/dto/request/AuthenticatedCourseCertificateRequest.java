package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticatedCourseCertificateRequest {

    private CoursePayload course;

    @JsonProperty("source_system")
    private String sourceSystem;

    public AuthenticatedCourseCertificateRequest() {
    }

    public AuthenticatedCourseCertificateRequest(CoursePayload course, String sourceSystem) {
        this.course = course;
        this.sourceSystem = sourceSystem;
    }

    public CoursePayload getCourse() {
        return course;
    }

    public void setCourse(CoursePayload course) {
        this.course = course;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
}
