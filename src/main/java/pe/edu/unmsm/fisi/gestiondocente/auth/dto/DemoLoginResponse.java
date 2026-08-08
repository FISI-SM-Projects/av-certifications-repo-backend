package pe.edu.unmsm.fisi.gestiondocente.auth.dto;

import pe.edu.unmsm.fisi.gestiondocente.usuario.dto.UsuarioSesionDto;

public class DemoLoginResponse {

    private UsuarioSesionDto user;

    public DemoLoginResponse() {
    }

    public DemoLoginResponse(UsuarioSesionDto user) {
        this.user = user;
    }

    public UsuarioSesionDto getUser() {
        return user;
    }

    public void setUser(UsuarioSesionDto user) {
        this.user = user;
    }
}
