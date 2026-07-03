package pe.edu.unmsm.fisi.gestiondocente.docente.dto;

import java.util.List;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.ConstanciaDto;

public class DocentePerfilResponse {

    private DocenteDto docente;
    private List<ConstanciaDto> constancias;

    public DocentePerfilResponse() {
    }

    public DocentePerfilResponse(DocenteDto docente, List<ConstanciaDto> constancias) {
        this.docente = docente;
        this.constancias = constancias;
    }

    public DocenteDto getDocente() {
        return docente;
    }

    public void setDocente(DocenteDto docente) {
        this.docente = docente;
    }

    public List<ConstanciaDto> getConstancias() {
        return constancias;
    }

    public void setConstancias(List<ConstanciaDto> constancias) {
        this.constancias = constancias;
    }
}
