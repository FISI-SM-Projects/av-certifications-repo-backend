package pe.edu.unmsm.fisi.gestiondocente.periodo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_period")
public class AcademicPeriod {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "semester_code", nullable = false, unique = true, length = 10)
    private String semesterCode;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "start_date", nullable = false)
    private java.time.LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private java.time.LocalDate endDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSemesterCode() { return semesterCode; }
    public void setSemesterCode(String semesterCode) { this.semesterCode = semesterCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public java.time.LocalDate getStartDate() { return startDate; }
    public void setStartDate(java.time.LocalDate startDate) { this.startDate = startDate; }

    public java.time.LocalDate getEndDate() { return endDate; }
    public void setEndDate(java.time.LocalDate endDate) { this.endDate = endDate; }
}
