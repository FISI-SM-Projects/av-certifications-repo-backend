package pe.edu.unmsm.fisi.gestiondocente.auth.dto;

public class AuthResponse {
    private final String jwt;
    public AuthResponse(String jwt) { this.jwt = jwt; }
    public String getJwt() { return jwt; }
}