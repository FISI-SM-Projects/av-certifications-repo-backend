package pe.edu.unmsm.fisi.gestiondocente.docente.repository;

import java.util.List;
import java.util.Locale;
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
            ),
            new Docente(
                    4L,
                    "22200275",
                    "Jos\u00e9 Mu\u00f1oz",
                    "Pe\u00f1a",
                    "jmunoz@unmsm.edu.pe",
                    "Aula Virtual Simulado",
                    "Demo",
                    "Simulado"
            ),
            new Docente(
                    5L,
                    "22200999",
                    "Ana",
                    "Torres Lima",
                    "atorres@unmsm.edu.pe",
                    "Aula Virtual Simulado",
                    "Demo",
                    "Simulado"
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
        String normalizedCodigo = codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);

        return DOCENTES_DEMO.stream()
                .filter(docente -> docente.getCodigo().equalsIgnoreCase(normalizedCodigo))
                .findFirst();
    }
}
