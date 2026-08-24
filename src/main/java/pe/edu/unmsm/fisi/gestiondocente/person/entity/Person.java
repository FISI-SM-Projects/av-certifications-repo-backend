package pe.edu.unmsm.fisi.gestiondocente.person.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.AccountStatus;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.PersonSystemRole;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "person")
@Getter
@Setter
@ToString(exclude = "personSystemRoles")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "first_name", nullable = false, length = 120)
    private String firstName;

    @Column(name = "paternal_last_name", nullable = false, length = 80)
    private String paternalLastName;

    @Column(name = "maternal_last_name", length = 80)
    private String maternalLastName;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "register_state", nullable = false, length = 20)
    private AccountStatus registerState;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PersonSystemRole> personSystemRoles = new HashSet<>();

    public String getFullName () {
        String maternal = (this.maternalLastName != null) ? " " + this.maternalLastName : "";
        return this.firstName + " " + this.paternalLastName + maternal;
    }
}
