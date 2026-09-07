package pe.edu.unmsm.fisi.gestiondocente.academic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import pe.edu.unmsm.fisi.gestiondocente.teacher.entity.Teacher;

@Entity
@Table(name = "academic_workload")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriod academicPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "moodle_id", nullable = false, unique = true)
    private Long moodleId;

    @Column(name = "cycle", nullable = false)
    private Integer cycle;

    @Column(name = "section", nullable = false)
    private Integer section;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "school", nullable = false, length = 2)
    private School school;

    @Column(name = "plan", nullable = false)
    private Integer plan;
}