package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request;

public class ExpectedCourseRequest {

    private String code;
    private String section;

    public ExpectedCourseRequest() {
    }

    public ExpectedCourseRequest(String code, String section) {
        this.code = code;
        this.section = section;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }
}
