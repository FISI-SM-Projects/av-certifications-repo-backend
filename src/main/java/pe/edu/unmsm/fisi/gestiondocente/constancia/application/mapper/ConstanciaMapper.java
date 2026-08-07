package pe.edu.unmsm.fisi.gestiondocente.constancia.application.mapper;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.constancia.web.dto.ConstanciaDto;
import pe.edu.unmsm.fisi.gestiondocente.constancia.domain.Constancia;
import pe.edu.unmsm.fisi.gestiondocente.periodo.domain.PeriodoAcademico;

@Component
public class ConstanciaMapper {

    public ConstanciaDto toDto(Constancia constancia, PeriodoAcademico periodo) {
        return new ConstanciaDto(
                constancia.getId(),
                constancia.getTitulo(),
                periodo.getNombre(),
                constancia.getEstado().name(),
                constancia.getFechaGeneracion(),
                constancia.getArchivoUrl()
        );
    }
}
