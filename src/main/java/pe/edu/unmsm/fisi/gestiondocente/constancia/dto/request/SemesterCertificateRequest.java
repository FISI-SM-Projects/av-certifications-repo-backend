package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SemesterCertificateRequest {

    @JsonProperty("teacher_code")
    private String teacherCode;

    private String semester;

    @JsonProperty("expected_courses")
    private List<ExpectedCourseRequest> expectedCourses;

    public SemesterCertificateRequest() {
    }

    public SemesterCertificateRequest(String teacherCode, String semester,
            List<ExpectedCourseRequest> expectedCourses) {
        this.teacherCode = teacherCode;
        this.semester = semester;
        this.expectedCourses = expectedCourses;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
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
