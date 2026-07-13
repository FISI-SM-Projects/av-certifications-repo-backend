package pe.edu.unmsm.fisi.gestiondocente.docente.dto;

public class DocenteListadoDto {

    private String teacherCode;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;
    private String departamentoAcademico;
    private String categoria;
    private String condicion;

    public DocenteListadoDto() {
    }

    public DocenteListadoDto(String teacherCode, String nombres, String apellidos, String correoInstitucional,
            String departamentoAcademico, String categoria, String condicion) {
        this.teacherCode = teacherCode;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correoInstitucional = correoInstitucional;
        this.departamentoAcademico = departamentoAcademico;
        this.categoria = categoria;
        this.condicion = condicion;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getDepartamentoAcademico() {
        return departamentoAcademico;
    }

    public void setDepartamentoAcademico(String departamentoAcademico) {
        this.departamentoAcademico = departamentoAcademico;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }
}
