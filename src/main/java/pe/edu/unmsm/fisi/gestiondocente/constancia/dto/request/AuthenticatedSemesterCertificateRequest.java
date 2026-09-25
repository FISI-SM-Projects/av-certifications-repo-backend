package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticatedSemesterCertificateRequest {

    private String semester;

    @JsonProperty("expected_courses")
    private List<ExpectedCourseRequest> expectedCourses;

    public AuthenticatedSemesterCertificateRequest() {
    }

    public AuthenticatedSemesterCertificateRequest(String semester, List<ExpectedCourseRequest> expectedCourses) {
        this.semester = semester;
        this.expectedCourses = expectedCourses;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public List<ExpectedCourseRequest> getExpectedCourses() {
        return expectedCourses;
    }

    public void setExpectedCourses(List<ExpectedCourseRequest> expectedCourses) {
        this.expectedCourses = expectedCourses;
    }
}
