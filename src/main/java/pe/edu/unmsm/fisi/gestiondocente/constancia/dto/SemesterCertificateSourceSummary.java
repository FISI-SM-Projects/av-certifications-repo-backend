package pe.edu.unmsm.fisi.gestiondocente.constancia.dto;

import java.util.List;

public class SemesterCertificateSourceSummary {

    private String teacherCode;
    private String teacherFullName;
    private String teacherEmail;
    private String semester;
    private List<SemesterCertificateSource> sourceGenerations;

    public SemesterCertificateSourceSummary() {
    }

    public SemesterCertificateSourceSummary(String teacherCode, String teacherFullName, String teacherEmail,
            String semester, List<SemesterCertificateSource> sourceGenerations) {
        this.teacherCode = teacherCode;
        this.teacherFullName = teacherFullName;
        this.teacherEmail = teacherEmail;
        this.semester = semester;
        this.sourceGenerations = List.copyOf(sourceGenerations);
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    public String getTeacherFullName() {
        return teacherFullName;
    }

    public void setTeacherFullName(String teacherFullName) {
        this.teacherFullName = teacherFullName;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public List<SemesterCertificateSource> getSourceGenerations() {
        return sourceGenerations;
    }

    public void setSourceGenerations(List<SemesterCertificateSource> sourceGenerations) {
        this.sourceGenerations = sourceGenerations;
    }
}
