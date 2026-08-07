package pe.edu.unmsm.fisi.gestiondocente.docente.application.mapper;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.docente.web.dto.DocenteDto;
import pe.edu.unmsm.fisi.gestiondocente.docente.web.dto.DocenteListadoDto;
import pe.edu.unmsm.fisi.gestiondocente.docente.domain.Docente;

@Component
public class DocenteMapper {

    public DocenteDto toDto(Docente docente) {
        return new DocenteDto(
                docente.getId(),
                docente.getCodigo(),
                docente.getNombres(),
                docente.getApellidos(),
                docente.getCorreoInstitucional(),
                docente.getDepartamentoAcademico(),
                docente.getCategoria(),
                docente.getCondicion()
        );
    }

    public DocenteListadoDto toListadoDto(Docente docente) {
        return new DocenteListadoDto(
                docente.getCodigo(),
                docente.getNombres(),
                docente.getApellidos(),
                docente.getCorreoInstitucional(),
                docente.getDepartamentoAcademico(),
                docente.getCategoria(),
                docente.getCondicion()
        );
    }
}
