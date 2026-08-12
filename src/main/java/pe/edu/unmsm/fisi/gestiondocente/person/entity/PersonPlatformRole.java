package pe.edu.unmsm.fisi.gestiondocente.person.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "persona_rol_plataforma", schema = "auth")
@Getter
@Setter
@ToString(exclude = {"person", "role"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonPlatformRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private PlatformRole role;

    @Column(name = "unidad_alcance_id")
    private Long scopeUnitId;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate startDate;

    @Column(name = "fecha_fin")
    private LocalDate endDate;

    @Column(name = "concedido_por_persona_id")
    private Long grantedByPersonId;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
