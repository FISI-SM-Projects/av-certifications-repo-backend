package pe.edu.unmsm.fisi.gestiondocente.constancia.entity;

import java.time.LocalDate;

public class Constancia {

    private Long id;
    private String titulo;
    private EstadoConstancia estado;
    private LocalDate fechaGeneracion;
    private String archivoUrl;
    private Long docenteId;
    private Long periodoId;

    public Constancia() {
    }

    public Constancia(Long id, String titulo, EstadoConstancia estado, LocalDate fechaGeneracion, String archivoUrl,
            Long docenteId, Long periodoId) {
        this.id = id;
        this.titulo = titulo;
        this.estado = estado;
        this.fechaGeneracion = fechaGeneracion;
        this.archivoUrl = archivoUrl;
        this.docenteId = docenteId;
        this.periodoId = periodoId;
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

    public EstadoConstancia getEstado() {
        return estado;
    }

    public void setEstado(EstadoConstancia estado) {
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

    public Long getDocenteId() {
        return docenteId;
    }

    public void setDocenteId(Long docenteId) {
        this.docenteId = docenteId;
    }

    public Long getPeriodoId() {
        return periodoId;
    }

    public void setPeriodoId(Long periodoId) {
        this.periodoId = periodoId;
    }
}
