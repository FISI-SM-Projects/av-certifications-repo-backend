package pe.edu.unmsm.fisi.gestiondocente.person.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "persona", schema = "auth")
@Getter
@Setter
@ToString(exclude = "personRoles")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "persona_id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "nombres", nullable = false, length = 120)
    private String firstName;

    @Column(name = "apellido_paterno", nullable = false, length = 80)
    private String paternalLastName;

    @Column(name = "apellido_materno", length = 80)
    private String maternalLastName;

    @Column(name = "nombre_preferido", length = 120)
    private String preferredName;

    @Column(name = "estado_registro", nullable = false, length = 20)
    private String registerState;

    @Column(name = "dni", nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PersonPlatformRole> personRoles = new HashSet<>();

    public String getFullName () {
        String maternal = (this.maternalLastName != null) ? " " + this.maternalLastName : "";
        return this.firstName + " " + this.paternalLastName + maternal;
    }
}
