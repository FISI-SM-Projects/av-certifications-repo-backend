package pe.edu.unmsm.fisi.gestiondocente.usuario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import pe.edu.unmsm.fisi.gestiondocente.usuario.entity.RolUsuario;
import pe.edu.unmsm.fisi.gestiondocente.usuario.entity.Usuario;

@Repository
public class UsuarioRepository {

    private static final List<Usuario> USUARIOS_DEMO = List.of(
            new Usuario(1L, "082026", null, null, RolUsuario.DOCENTE, null, "082026"),
            new Usuario(2L, "082027", null, null, RolUsuario.DOCENTE, null, "082027"),
            new Usuario(3L, "082028", null, null, RolUsuario.DOCENTE, null, "082028"),
            new Usuario(
                    4L,
                    "DIR-ISW",
                    "Director de Ingenier\u00eda de Software",
                    "director.software@unmsm.edu.pe",
                    RolUsuario.DIRECTOR,
                    "Ingenier\u00eda de Software",
                    null),
            new Usuario(
                    5L,
                    "DIR-CC",
                    "Director de Ciencia de la Computaci\u00f3n",
                    "director.computacion@unmsm.edu.pe",
                    RolUsuario.DIRECTOR,
                    "Ciencia de la Computaci\u00f3n",
                    null),
            new Usuario(
                    6L,
                    "ADMIN-01",
                    "Administrador del Sistema",
                    "admin@unmsm.edu.pe",
                    RolUsuario.ADMIN,
                    null,
                    null)
    );

    public List<Usuario> findAll() {
        return USUARIOS_DEMO.stream()
                .map(UsuarioRepository::copyOf)
                .toList();
    }

    public Optional<Usuario> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        String emailBuscado = email.trim();

        return USUARIOS_DEMO.stream()
                .filter(usuario -> usuario.getEmail() != null)
                .filter(usuario -> usuario.getEmail().equalsIgnoreCase(emailBuscado))
                .findFirst()
                .map(UsuarioRepository::copyOf);
    }

    public Optional<Usuario> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return USUARIOS_DEMO.stream()
                .filter(usuario -> usuario.getId().equals(id))
                .findFirst()
                .map(UsuarioRepository::copyOf);
    }

    private static Usuario copyOf(Usuario usuario) {
        return new Usuario(
                usuario.getId(),
                usuario.getCodigo(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getDepartamentoAcademico(),
                usuario.getTeacherCode());
    }
}
