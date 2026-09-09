package pe.edu.unmsm.fisi.gestiondocente.constancia.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "certification")
public class Certification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "academic_workload_id")
    private pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity.AcademicWorkload academicWorkload;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "teacher_id")
    private pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "academic_period_id")
    private pe.edu.unmsm.fisi.gestiondocente.periodo.entity.AcademicPeriod academicPeriod;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcType(org.hibernate.dialect.PostgreSQLEnumJdbcType.class)
    @Column(name = "certificate_type", nullable = false)
    private CertificationType certificateType = CertificationType.COURSE;

    @Column(name = "document_path", nullable = false, length = 500)
    private String documentPath;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

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

    public pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher getTeacher() { return teacher; }
    public void setTeacher(pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher teacher) { this.teacher = teacher; }

    public pe.edu.unmsm.fisi.gestiondocente.periodo.entity.AcademicPeriod getAcademicPeriod() { return academicPeriod; }
    public void setAcademicPeriod(pe.edu.unmsm.fisi.gestiondocente.periodo.entity.AcademicPeriod academicPeriod) { this.academicPeriod = academicPeriod; }

    public CertificationType getCertificateType() { return certificateType; }
    public void setCertificateType(CertificationType certificateType) { this.certificateType = certificateType; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public CertificationStatus getStatus() { return status; }
    public void setStatus(CertificationStatus status) { this.status = status; }
}
