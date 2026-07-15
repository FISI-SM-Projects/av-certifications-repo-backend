package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeacherPayload {

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @JsonProperty("teacher_code")
    private String teacherCode;

    public TeacherPayload() {
    }

    public TeacherPayload(String fullName, String email, String teacherCode) {
        this.fullName = fullName;
        this.email = email;
        this.teacherCode = teacherCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }
}
