package pe.edu.unmsm.fisi.gestiondocente.auth.dto;

public record LoginResponse (
    String token,
    String type,
    long expiresIn
) {
    public LoginResponse(String token, long expiresIn) {
        this(token, "Bearer", expiresIn);
    }
}
