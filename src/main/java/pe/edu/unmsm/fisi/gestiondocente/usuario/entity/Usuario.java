package pe.edu.unmsm.fisi.gestiondocente.usuario.entity;

public class Usuario {

    private Long id;
    private String codigo;
    private String nombreCompleto;
    private String email;
    private RolUsuario rol;
    private String departamentoAcademico;
    private String teacherCode;

    public Usuario() {
    }

    public Usuario(Long id, String codigo, String nombreCompleto, String email, RolUsuario rol,
            String departamentoAcademico, String teacherCode) {
        this.id = id;
        this.codigo = codigo;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.rol = rol;
        this.departamentoAcademico = departamentoAcademico;
        this.teacherCode = teacherCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public String getDepartamentoAcademico() {
        return departamentoAcademico;
    }

    public void setDepartamentoAcademico(String departamentoAcademico) {
        this.departamentoAcademico = departamentoAcademico;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }
}
