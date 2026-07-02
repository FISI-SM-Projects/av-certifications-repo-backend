package pe.edu.unmsm.fisi.gestiondocente.constancia.mapper;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.ConstanciaDto;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.Constancia;
import pe.edu.unmsm.fisi.gestiondocente.periodo.entity.PeriodoAcademico;

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
