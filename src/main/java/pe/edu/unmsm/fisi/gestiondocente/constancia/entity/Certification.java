package pe.edu.unmsm.fisi.gestiondocente.constancia.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "certification")
public class Certification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "academic_workload_id", nullable = false)
    private pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity.AcademicWorkload academicWorkload;

    @Column(name = "document_path", nullable = false, length = 500)
    private String documentPath;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcType(org.hibernate.dialect.PostgreSQLEnumJdbcType.class)
    private CertificationStatus status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity.AcademicWorkload getAcademicWorkload() { return academicWorkload; }
    public void setAcademicWorkload(pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity.AcademicWorkload academicWorkload) { this.academicWorkload = academicWorkload; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public CertificationStatus getStatus() { return status; }
    public void setStatus(CertificationStatus status) { this.status = status; }
}
