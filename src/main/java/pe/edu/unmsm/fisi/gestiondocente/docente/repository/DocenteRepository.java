package pe.edu.unmsm.fisi.gestiondocente.docente.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Docente;

@Repository
public class DocenteRepository {

    public Optional<Docente> findDemoDocente() {
        return Optional.of(new Docente(
                1L,
                "082026",
                "Juan Carlos",
                "Pérez Gómez",
                "jperez@unmsm.edu.pe",
                "Ingeniería de Software",
                "Asociado",
                "Nombrado"
        ));
    }
}
