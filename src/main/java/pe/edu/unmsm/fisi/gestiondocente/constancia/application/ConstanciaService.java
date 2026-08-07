package pe.edu.unmsm.fisi.gestiondocente.constancia.application;

import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.constancia.web.dto.ConstanciaDto;
import pe.edu.unmsm.fisi.gestiondocente.constancia.domain.Constancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.application.mapper.ConstanciaMapper;
import pe.edu.unmsm.fisi.gestiondocente.constancia.infrastructure.persistence.LegacyConstanciaRepository;
import pe.edu.unmsm.fisi.gestiondocente.periodo.domain.PeriodoAcademico;
import pe.edu.unmsm.fisi.gestiondocente.periodo.infrastructure.repository.PeriodoAcademicoRepository;

@Service
public class ConstanciaService {

    private static final String PERIODO_NO_DEFINIDO = "Periodo no definido";

    private final LegacyConstanciaRepository constanciaRepository;
    private final PeriodoAcademicoRepository periodoAcademicoRepository;
    private final ConstanciaMapper constanciaMapper;

    public ConstanciaService(LegacyConstanciaRepository constanciaRepository,
            PeriodoAcademicoRepository periodoAcademicoRepository, ConstanciaMapper constanciaMapper) {
        this.constanciaRepository = constanciaRepository;
        this.periodoAcademicoRepository = periodoAcademicoRepository;
        this.constanciaMapper = constanciaMapper;
    }

    public List<ConstanciaDto> obtenerConstanciasPorDocente(Long docenteId) {
        return constanciaRepository.findByDocenteId(docenteId).stream()
                .map(this::toDto)
                .toList();
    }

    private ConstanciaDto toDto(Constancia constancia) {
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(constancia.getPeriodoId())
                .orElseGet(() -> new PeriodoAcademico(null, PERIODO_NO_DEFINIDO, null, null, false));

        return constanciaMapper.toDto(constancia, periodo);
    }
}
