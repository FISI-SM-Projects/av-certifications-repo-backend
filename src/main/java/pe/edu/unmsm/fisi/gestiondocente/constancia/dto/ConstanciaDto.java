package pe.edu.unmsm.fisi.gestiondocente.constancia.dto;

import java.time.LocalDate;

public class ConstanciaDto {

    private Long id;
    private String titulo;
    private String periodo;
    private String estado;
    private LocalDate fechaGeneracion;
    private String archivoUrl;

    public ConstanciaDto() {
    }

    public ConstanciaDto(Long id, String titulo, String periodo, String estado, LocalDate fechaGeneracion,
            String archivoUrl) {
        this.id = id;
        this.titulo = titulo;
        this.periodo = periodo;
        this.estado = estado;
        this.fechaGeneracion = fechaGeneracion;
        this.archivoUrl = archivoUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getArchivoUrl() {
        return archivoUrl;
    }

    public void setArchivoUrl(String archivoUrl) {
        this.archivoUrl = archivoUrl;
    }
}
