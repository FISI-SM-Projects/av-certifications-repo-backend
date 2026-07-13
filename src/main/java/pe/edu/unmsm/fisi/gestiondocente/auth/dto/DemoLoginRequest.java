package pe.edu.unmsm.fisi.gestiondocente.auth.dto;

public class DemoLoginRequest {

    private String email;

    public DemoLoginRequest() {
    }

    public DemoLoginRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
