package pe.edu.unmsm.fisi.gestiondocente.constancia.repository;

import java.util.List;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.Constancia;

public interface LegacyConstanciaRepository {

    List<Constancia> findByDocenteId(Long docenteId);
}
