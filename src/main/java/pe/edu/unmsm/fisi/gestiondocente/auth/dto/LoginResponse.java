package pe.edu.unmsm.fisi.gestiondocente.auth.dto;

public record LoginResponse(
    String token,
    String type
) {
    public LoginResponse(String token) {
        this(token, "Bearer");
    }
}
