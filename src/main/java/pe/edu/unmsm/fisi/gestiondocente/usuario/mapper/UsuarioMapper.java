package pe.edu.unmsm.fisi.gestiondocente.usuario.mapper;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.usuario.dto.UsuarioSesionDto;
import pe.edu.unmsm.fisi.gestiondocente.usuario.entity.Usuario;

@Component
public class UsuarioMapper {

    public UsuarioSesionDto toSesionDto(Usuario usuario) {
        return new UsuarioSesionDto(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getDepartamentoAcademico(),
                usuario.getTeacherCode()
        );
    }
}
