package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request;

public class CoursePayload {

    private String code;
    private String subject;
    private String cycle;
    private String section;
    private String school;
    private String plan;
    private String semester;

    public CoursePayload() {
    }

    public CoursePayload(String code, String subject, String cycle, String section, String school, String plan,
            String semester) {
        this.code = code;
        this.subject = subject;
        this.cycle = cycle;
        this.section = section;
        this.school = school;
        this.plan = plan;
        this.semester = semester;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
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

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
}
