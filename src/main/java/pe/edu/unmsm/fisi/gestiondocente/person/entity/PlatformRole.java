package pe.edu.unmsm.fisi.gestiondocente.person.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rol_plataforma", schema = "auth")
@Getter
@Setter
@ToString(exclude = "permissions")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 60)
    private String code;

    @Column(name = "nombre", nullable = false, length = 140)
    private String name;

    @Column(name = "descripcion", length = 400)
    private String description;

    @Column(name = "activo", nullable = false)
    private Boolean active;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rol_permiso",
            schema = "auth",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @Builder.Default
    private Set<PlatformPermission> permissions = new HashSet<>();
}
