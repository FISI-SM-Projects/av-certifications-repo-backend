package pe.edu.unmsm.fisi.gestiondocente.repository.constancia;

import java.util.List;

import pe.edu.unmsm.fisi.gestiondocente.entity.constancia.Constancia;

public interface LegacyConstanciaRepository {

    List<Constancia> findByDocenteId(Long docenteId);
}
