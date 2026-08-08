package pe.edu.unmsm.fisi.gestiondocente.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.auth.dto.DemoLoginRequest;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.DemoLoginResponse;
import pe.edu.unmsm.fisi.gestiondocente.auth.exception.DemoUserNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.DocenteRepository;
import pe.edu.unmsm.fisi.gestiondocente.usuario.dto.UsuarioSesionDto;
import pe.edu.unmsm.fisi.gestiondocente.usuario.entity.Usuario;
import pe.edu.unmsm.fisi.gestiondocente.usuario.mapper.UsuarioMapper;
import pe.edu.unmsm.fisi.gestiondocente.usuario.repository.UsuarioRepository;

@Service
public class DemoAuthService {

    private static final String MENSAJE_CORREO_OBLIGATORIO = "El correo es obligatorio";
    private static final String MENSAJE_USUARIO_NO_ENCONTRADO = "Usuario demo no encontrado";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final DocenteRepository docenteRepository;

    public DemoAuthService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this(usuarioRepository, usuarioMapper, new DocenteRepository());
    }

    @Autowired
    public DemoAuthService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper,
            DocenteRepository docenteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.docenteRepository = docenteRepository;
    }

    public List<UsuarioSesionDto> listarUsuariosDemo() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toSesionDto)
                .toList();
    }

    public DemoLoginResponse login(DemoLoginRequest request) {
        String email = obtenerEmailNormalizado(request);

        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(candidate -> matchesLoginEmail(candidate, email))
                .findFirst()
                .orElseThrow(() -> new DemoUserNotFoundException(MENSAJE_USUARIO_NO_ENCONTRADO));

        return new DemoLoginResponse(usuarioMapper.toSesionDto(usuario));
    }

    private boolean matchesLoginEmail(Usuario usuario, String email) {
        if (usuario.getTeacherCode() != null) {
            return docenteRepository.findByCodigo(usuario.getTeacherCode())
                    .map(docente -> docente.getCorreoInstitucional().equalsIgnoreCase(email))
                    .orElse(false);
        }

        return usuario.getEmail() != null && usuario.getEmail().equalsIgnoreCase(email);
    }

    private String obtenerEmailNormalizado(DemoLoginRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(MENSAJE_CORREO_OBLIGATORIO);
        }

        return request.getEmail().trim();
    }
}
