package pe.edu.unmsm.fisi.gestiondocente.mapper.usuario;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.entity.docente.Docente;
import pe.edu.unmsm.fisi.gestiondocente.repository.docente.DocenteRepository;
import pe.edu.unmsm.fisi.gestiondocente.dto.usuario.UsuarioSesionDto;
import pe.edu.unmsm.fisi.gestiondocente.entity.usuario.Usuario;

@Component
public class UsuarioMapper {

    private final DocenteRepository docenteRepository;

    public UsuarioMapper() {
        this(new DocenteRepository());
    }

    public UsuarioMapper(DocenteRepository docenteRepository) {
        this.docenteRepository = docenteRepository;
    }

    public UsuarioSesionDto toSesionDto(Usuario usuario) {
        if (usuario.getTeacherCode() != null) {
            return docenteRepository.findByCodigo(usuario.getTeacherCode())
                    .map(docente -> toSesionDto(usuario, docente))
                    .orElseGet(() -> toSesionDtoFromUsuario(usuario));
        }

        return toSesionDtoFromUsuario(usuario);
    }

    private UsuarioSesionDto toSesionDto(Usuario usuario, Docente docente) {
        return new UsuarioSesionDto(
                usuario.getId(),
                docente.getNombres() + " " + docente.getApellidos(),
                docente.getCorreoInstitucional(),
                usuario.getRol(),
                docente.getDepartamentoAcademico(),
                usuario.getTeacherCode()
        );
    }

    private UsuarioSesionDto toSesionDtoFromUsuario(Usuario usuario) {
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
