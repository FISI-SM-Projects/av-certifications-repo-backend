package pe.edu.unmsm.fisi.gestiondocente.usuario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import pe.edu.unmsm.fisi.gestiondocente.usuario.entity.RolUsuario;
import pe.edu.unmsm.fisi.gestiondocente.usuario.entity.Usuario;

@Repository
public class UsuarioRepository {

    private static final List<Usuario> USUARIOS_DEMO = List.of(
            new Usuario(
                    1L,
                    "082026",
                    "Juan Carlos Pérez Gómez",
                    "jperez@unmsm.edu.pe",
                    RolUsuario.DOCENTE,
                    "Ingeniería de Software",
                    "082026"
            ),
            new Usuario(
                    2L,
                    "082027",
                    "María Elena Torres Rojas",
                    "mtorres@unmsm.edu.pe",
                    RolUsuario.DOCENTE,
                    "Ingeniería de Software",
                    "082027"
            ),
            new Usuario(
                    3L,
                    "082028",
                    "Carlos Alberto Ramos Silva",
                    "cramos@unmsm.edu.pe",
                    RolUsuario.DOCENTE,
                    "Ciencia de la Computación",
                    "082028"
            ),
            new Usuario(
                    4L,
                    "DIR-ISW",
                    "Director de Ingeniería de Software",
                    "director.software@unmsm.edu.pe",
                    RolUsuario.DIRECTOR,
                    "Ingeniería de Software",
                    null
            ),
            new Usuario(
                    5L,
                    "DIR-CC",
                    "Director de Ciencia de la Computación",
                    "director.computacion@unmsm.edu.pe",
                    RolUsuario.DIRECTOR,
                    "Ciencia de la Computación",
                    null
            ),
            new Usuario(
                    6L,
                    "ADMIN-01",
                    "Administrador del Sistema",
                    "admin@unmsm.edu.pe",
                    RolUsuario.ADMIN,
                    null,
                    null
            )
    );

    public List<Usuario> findAll() {
        return USUARIOS_DEMO;
    }

    public Optional<Usuario> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        String emailBuscado = email.trim();

        for (Usuario usuario : USUARIOS_DEMO) {
            if (usuario.getEmail().equalsIgnoreCase(emailBuscado)) {
                return Optional.of(usuario);
            }
        }

        return Optional.empty();
    }

    public Optional<Usuario> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        for (Usuario usuario : USUARIOS_DEMO) {
            if (usuario.getId().equals(id)) {
                return Optional.of(usuario);
            }
        }

        return Optional.empty();
    }
}
