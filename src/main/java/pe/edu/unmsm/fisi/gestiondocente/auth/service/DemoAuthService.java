package pe.edu.unmsm.fisi.gestiondocente.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.auth.dto.DemoLoginRequest;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.DemoLoginResponse;
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

    public DemoAuthService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    public List<UsuarioSesionDto> listarUsuariosDemo() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toSesionDto)
                .toList();
    }

    public DemoLoginResponse login(DemoLoginRequest request) {
        String email = obtenerEmailNormalizado(request);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(MENSAJE_USUARIO_NO_ENCONTRADO));

        return new DemoLoginResponse(usuarioMapper.toSesionDto(usuario));
    }

    private String obtenerEmailNormalizado(DemoLoginRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(MENSAJE_CORREO_OBLIGATORIO);
        }

        return request.getEmail().trim();
    }
}
