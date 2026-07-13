package pe.edu.unmsm.fisi.gestiondocente.usuario.dto;

import pe.edu.unmsm.fisi.gestiondocente.usuario.entity.RolUsuario;

public class UsuarioSesionDto {

    private Long id;
    private String fullName;
    private String email;
    private RolUsuario role;
    private String departamentoAcademico;
    private String teacherCode;

    public UsuarioSesionDto() {
    }

    public UsuarioSesionDto(Long id, String fullName, String email, RolUsuario role,
            String departamentoAcademico, String teacherCode) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.departamentoAcademico = departamentoAcademico;
        this.teacherCode = teacherCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RolUsuario getRole() {
        return role;
    }

    public void setRole(RolUsuario role) {
        this.role = role;
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
