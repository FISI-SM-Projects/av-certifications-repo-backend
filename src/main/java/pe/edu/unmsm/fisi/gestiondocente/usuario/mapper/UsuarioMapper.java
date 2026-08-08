package pe.edu.unmsm.fisi.gestiondocente.usuario.mapper;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Docente;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.DocenteRepository;
import pe.edu.unmsm.fisi.gestiondocente.usuario.dto.UsuarioSesionDto;
import pe.edu.unmsm.fisi.gestiondocente.usuario.entity.Usuario;

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
