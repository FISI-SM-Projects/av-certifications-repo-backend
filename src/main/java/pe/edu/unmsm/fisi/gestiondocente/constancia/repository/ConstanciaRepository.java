package pe.edu.unmsm.fisi.gestiondocente.constancia.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.Constancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;

@Repository
public class ConstanciaRepository {

    public List<Constancia> findByDocenteId(Long docenteId) {
        return List.of(
                new Constancia(
                        1L,
                        "Constancia de cumplimiento en Aula Virtual",
                        EstadoConstancia.GENERADO,
                        LocalDate.of(2026, 6, 20),
                        "/constancias/demo-2026-I.pdf",
                        1L,
                        1L
                ),
                new Constancia(
                        2L,
                        "Constancia de cumplimiento en Aula Virtual",
                        EstadoConstancia.APROBADO,
                        LocalDate.of(2025, 12, 10),
                        "/constancias/demo-2025-II.pdf",
                        1L,
                        2L
                )
        ).stream()
                .filter(constancia -> constancia.getDocenteId().equals(docenteId))
                .toList();
    }
}
