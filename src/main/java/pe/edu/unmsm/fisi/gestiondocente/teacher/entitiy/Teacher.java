package pe.edu.unmsm.fisi.gestiondocente.teacher.entitiy;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;

@Entity
@Table(name = "teacher")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "moodle_id", unique = true)
    private Long moodleId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "department", length = 2)
    private Department department;
}
