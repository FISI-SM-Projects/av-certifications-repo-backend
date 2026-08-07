package pe.edu.unmsm.fisi.gestiondocente.constancia.infrastructure.persistence;

import java.util.List;

import pe.edu.unmsm.fisi.gestiondocente.constancia.domain.Constancia;

public interface LegacyConstanciaRepository {

    List<Constancia> findByDocenteId(Long docenteId);
}
