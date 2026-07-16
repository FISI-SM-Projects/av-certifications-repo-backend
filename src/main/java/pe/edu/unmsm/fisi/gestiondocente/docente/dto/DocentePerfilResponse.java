package pe.edu.unmsm.fisi.gestiondocente.docente.dto;

import java.util.List;

public class DocentePerfilResponse {

    private DocenteDto docente;
    private List<ConstanciaPerfilResponse> constancias;

    public DocentePerfilResponse() {
    }

    public DocentePerfilResponse(DocenteDto docente, List<ConstanciaPerfilResponse> constancias) {
        this.docente = docente;
        this.constancias = constancias;
    }

    public DocenteDto getDocente() {
        return docente;
    }

    public void setDocente(DocenteDto docente) {
        this.docente = docente;
    }

    public List<ConstanciaPerfilResponse> getConstancias() {
        return constancias;
    }

    public void setConstancias(List<ConstanciaPerfilResponse> constancias) {
        this.constancias = constancias;
    }
}
