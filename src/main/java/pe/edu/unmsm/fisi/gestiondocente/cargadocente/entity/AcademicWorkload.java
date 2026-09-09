package pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_workload")
public class AcademicWorkload {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "teacher_id", nullable = false)
    private pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "course_id", nullable = false)
    private pe.edu.unmsm.fisi.gestiondocente.curso.entity.Course course;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "academic_period_id", nullable = false)
    private pe.edu.unmsm.fisi.gestiondocente.periodo.entity.AcademicPeriod academicPeriod;

    @Column(name = "moodle_id", nullable = false, unique = true)
    private Long moodleId;

    @Column(nullable = false)
    private Integer cycle;

    @Column(nullable = false)
    private Integer section;

    @Column(nullable = false)
    private Integer plan;

    @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcType(org.hibernate.dialect.PostgreSQLEnumJdbcType.class) @Column(nullable = false)
    private School school;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher getTeacher() { return teacher; }
    public void setTeacher(pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher teacher) { this.teacher = teacher; }

    public pe.edu.unmsm.fisi.gestiondocente.curso.entity.Course getCourse() { return course; }
    public void setCourse(pe.edu.unmsm.fisi.gestiondocente.curso.entity.Course course) { this.course = course; }

    public pe.edu.unmsm.fisi.gestiondocente.periodo.entity.AcademicPeriod getAcademicPeriod() { return academicPeriod; }
    public void setAcademicPeriod(pe.edu.unmsm.fisi.gestiondocente.periodo.entity.AcademicPeriod academicPeriod) { this.academicPeriod = academicPeriod; }

    public Long getMoodleId() { return moodleId; }
    public void setMoodleId(Long moodleId) { this.moodleId = moodleId; }

    public Integer getCycle() { return cycle; }
    public void setCycle(Integer cycle) { this.cycle = cycle; }

    public Integer getSection() { return section; }
    public void setSection(Integer section) { this.section = section; }

    public Integer getPlan() { return plan; }
    public void setPlan(Integer plan) { this.plan = plan; }

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }
}
