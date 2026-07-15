package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request;

public class CourseCertificateRequest {

    private TeacherPayload teacher;
    private CoursePayload course;
    private IssuerPayload issuer;

    public CourseCertificateRequest() {
    }

    public CourseCertificateRequest(TeacherPayload teacher, CoursePayload course, IssuerPayload issuer) {
        this.teacher = teacher;
        this.course = course;
        this.issuer = issuer;
    }

    public TeacherPayload getTeacher() {
        return teacher;
    }

    public void setTeacher(TeacherPayload teacher) {
        this.teacher = teacher;
    }

    public CoursePayload getCourse() {
        return course;
    }

    public void setCourse(CoursePayload course) {
        this.course = course;
    }

    public IssuerPayload getIssuer() {
        return issuer;
    }

    public void setIssuer(IssuerPayload issuer) {
        this.issuer = issuer;
    }
}
