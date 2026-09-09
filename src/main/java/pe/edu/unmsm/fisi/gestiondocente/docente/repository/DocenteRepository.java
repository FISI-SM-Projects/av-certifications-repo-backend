package pe.edu.unmsm.fisi.gestiondocente.docente.repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Docente;

@Repository
@org.springframework.context.annotation.Profile("test")
public class DocenteRepository {

    private static final List<Docente> DOCENTES_TEST = List.of(
            new Docente(
                    1L,
                    "22200100",
                    "Lazaro Florian",
                    "Mota Alva",
                    "lmotaa@unmsm.edu.pe",
                    "Ciencia de la Computación",
                    "Asociado",
                    "Nombrado"
            ),
            new Docente(
                    2L,
                    "22200101",
                    "Luis Alberto",
                    "Alarcon Loayza",
                    "lalarconl@unmsm.edu.pe",
                    "Ciencia de la Computación",
                    "Auxiliar",
                    "Contratado"
            ),
            new Docente(
                    3L,
                    "22200102",
                    "Carlos Edmundo",
                    "Navarro Depaz",
                    "cnavarrod@unmsm.edu.pe",
                    "Ingeniería de Software",
                    "Asociado",
                    "Nombrado"
            ),
            new Docente(
                    4L,
                    "22200275",
                    "Jos\u00e9 Mu\u00f1oz",
                    "Pe\u00f1a",
                    "jmunoz@unmsm.edu.pe",
                    "Ciencia de la Computación",
                    "Asociado",
                    "Nombrado"
            ),
            new Docente(
                    5L,
                    "22200999",
                    "Ana",
                    "Torres Lima",
                    "atorres@unmsm.edu.pe",
                    "Ingeniería de Software",
                    "Auxiliar",
                    "Contratado"
            )
    );

    public List<Docente> findAll() {
        return DOCENTES_TEST.stream()
                .map(DocenteRepository::copyOf)
                .toList();
    }

    public Optional<Docente> findDefaultDocente() {
        return findByCodigo("22200101");
    }

    public List<Docente> findByDepartamentoAcademico(String departamentoAcademico) {
        return DOCENTES_TEST.stream()
                .filter(docente -> docente.getDepartamentoAcademico().equals(departamentoAcademico))
                .map(DocenteRepository::copyOf)
                .toList();
    }

    public Optional<Docente> findByCodigo(String codigo) {
        String normalizedCodigo = codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);

        return DOCENTES_TEST.stream()
                .filter(docente -> docente.getCodigo().equalsIgnoreCase(normalizedCodigo))
                .map(DocenteRepository::copyOf)
                .findFirst();
    }

    private static Docente copyOf(Docente docente) {
        return new Docente(
                docente.getId(),
                docente.getCodigo(),
                docente.getNombres(),
                docente.getApellidos(),
                docente.getCorreoInstitucional(),
                docente.getDepartamentoAcademico(),
                docente.getCategoria(),
                docente.getCondicion());
    }
}
