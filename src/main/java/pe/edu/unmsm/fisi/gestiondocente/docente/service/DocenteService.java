package pe.edu.unmsm.fisi.gestiondocente.docente.service;

import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.docente.dto.DocenteListadoDto;
import pe.edu.unmsm.fisi.gestiondocente.docente.mapper.DocenteMapper;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.DocenteRepository;

@Service
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final DocenteMapper docenteMapper;

    public DocenteService(DocenteRepository docenteRepository, DocenteMapper docenteMapper) {
        this.docenteRepository = docenteRepository;
        this.docenteMapper = docenteMapper;
    }

    public List<DocenteListadoDto> listarDocentesPorDepartamento(String departamentoAcademico) {
        if (departamentoAcademico == null || departamentoAcademico.trim().isEmpty()) {
            throw new IllegalArgumentException("El departamento académico es obligatorio");
        }

        return docenteRepository.findByDepartamentoAcademico(departamentoAcademico.trim()).stream()
                .map(docenteMapper::toListadoDto)
                .toList();
    }

}
