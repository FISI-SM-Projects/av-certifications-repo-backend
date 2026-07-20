package pe.edu.unmsm.fisi.gestiondocente.mapper.constancia;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.dto.constancia.ConstanciaDto;
import pe.edu.unmsm.fisi.gestiondocente.entity.constancia.Constancia;
import pe.edu.unmsm.fisi.gestiondocente.entity.periodo.PeriodoAcademico;

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
