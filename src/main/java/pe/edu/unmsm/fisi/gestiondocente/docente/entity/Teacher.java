package pe.edu.unmsm.fisi.gestiondocente.docente.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher")
public class Teacher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "person_id", nullable = false, unique = true)
    private pe.edu.unmsm.fisi.gestiondocente.person.entity.Person person;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "moodle_id", nullable = false, unique = true)
    private Long moodleId;

    @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcType(org.hibernate.dialect.PostgreSQLEnumJdbcType.class)
    private Department department;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public pe.edu.unmsm.fisi.gestiondocente.person.entity.Person getPerson() { return person; }
    public void setPerson(pe.edu.unmsm.fisi.gestiondocente.person.entity.Person person) { this.person = person; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getMoodleId() { return moodleId; }
    public void setMoodleId(Long moodleId) { this.moodleId = moodleId; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}
