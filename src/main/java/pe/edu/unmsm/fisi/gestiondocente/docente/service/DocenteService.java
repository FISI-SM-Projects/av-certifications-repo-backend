package pe.edu.unmsm.fisi.gestiondocente.docente.service;

import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.ConstanciaDto;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.ConstanciaService;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.DocenteDto;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.DocenteListadoDto;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.DocentePerfilResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Docente;
import pe.edu.unmsm.fisi.gestiondocente.docente.mapper.DocenteMapper;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.DocenteRepository;

@Service
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final DocenteMapper docenteMapper;
    private final ConstanciaService constanciaService;

    public DocenteService(DocenteRepository docenteRepository, DocenteMapper docenteMapper,
            ConstanciaService constanciaService) {
        this.docenteRepository = docenteRepository;
        this.docenteMapper = docenteMapper;
        this.constanciaService = constanciaService;
    }

    public DocentePerfilResponse obtenerPerfilDocenteDemo() {
        Docente docente = docenteRepository.findDemoDocente()
                .orElseThrow(() -> new IllegalStateException("No se encontro el docente demo"));
        return construirPerfilDocente(docente);
    }

    public List<DocenteListadoDto> listarDocentesPorDepartamento(String departamentoAcademico) {
        if (departamentoAcademico == null || departamentoAcademico.trim().isEmpty()) {
            throw new IllegalArgumentException("El departamento académico es obligatorio");
        }

        return docenteRepository.findByDepartamentoAcademico(departamentoAcademico.trim()).stream()
                .map(docenteMapper::toListadoDto)
                .toList();
    }

    public DocentePerfilResponse obtenerPerfilDocentePorCodigo(String teacherCode) {
        Docente docente = docenteRepository.findByCodigo(teacherCode)
                .orElseThrow(() -> new IllegalStateException("Docente no encontrado"));
        return construirPerfilDocente(docente);
    }

    private DocentePerfilResponse construirPerfilDocente(Docente docente) {
        DocenteDto docenteDto = docenteMapper.toDto(docente);
        List<ConstanciaDto> constancias = constanciaService.obtenerConstanciasPorDocente(docente.getId());

        return new DocentePerfilResponse(docenteDto, constancias);
    }
}
