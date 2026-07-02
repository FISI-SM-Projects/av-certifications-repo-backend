package pe.edu.unmsm.fisi.gestiondocente.periodo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import pe.edu.unmsm.fisi.gestiondocente.periodo.entity.PeriodoAcademico;

@Repository
public class PeriodoAcademicoRepository {

    public Optional<PeriodoAcademico> findById(Long id) {
        return periodosDemo().stream()
                .filter(periodo -> periodo.getId().equals(id))
                .findFirst();
    }

    private List<PeriodoAcademico> periodosDemo() {
        return List.of(
                new PeriodoAcademico(
                        1L,
                        "2026-I",
                        LocalDate.of(2026, 3, 30),
                        LocalDate.of(2026, 7, 18),
                        true
                ),
                new PeriodoAcademico(
                        2L,
                        "2025-II",
                        LocalDate.of(2025, 8, 18),
                        LocalDate.of(2025, 12, 13),
                        false
                )
        );
    }
}
