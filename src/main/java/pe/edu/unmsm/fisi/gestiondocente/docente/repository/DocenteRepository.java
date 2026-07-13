package pe.edu.unmsm.fisi.gestiondocente.docente.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Docente;

@Repository
public class DocenteRepository {

    private static final List<Docente> DOCENTES_DEMO = List.of(
            new Docente(
                    1L,
                    "082026",
                    "Juan Carlos",
                    "Pérez Gómez",
                    "jperez@unmsm.edu.pe",
                    "Ingeniería de Software",
                    "Asociado",
                    "Nombrado"
            ),
            new Docente(
                    2L,
                    "082027",
                    "María Elena",
                    "Torres Rojas",
                    "mtorres@unmsm.edu.pe",
                    "Ingeniería de Software",
                    "Auxiliar",
                    "Contratado"
            ),
            new Docente(
                    3L,
                    "082028",
                    "Carlos Alberto",
                    "Ramos Silva",
                    "cramos@unmsm.edu.pe",
                    "Ciencia de la Computación",
                    "Asociado",
                    "Nombrado"
            )
    );

    public List<Docente> findAll() {
        return DOCENTES_DEMO;
    }

    public Optional<Docente> findDemoDocente() {
        return findByCodigo("082026");
    }

    public List<Docente> findByDepartamentoAcademico(String departamentoAcademico) {
        return DOCENTES_DEMO.stream()
                .filter(docente -> docente.getDepartamentoAcademico().equals(departamentoAcademico))
                .toList();
    }

    public Optional<Docente> findByCodigo(String codigo) {
        return DOCENTES_DEMO.stream()
                .filter(docente -> docente.getCodigo().equals(codigo))
                .findFirst();
    }
}
