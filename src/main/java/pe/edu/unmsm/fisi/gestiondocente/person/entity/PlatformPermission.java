package pe.edu.unmsm.fisi.gestiondocente.person.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permiso_plataforma", schema = "auth")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 70)
    private String code;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "descripcion", length = 400)
    private String description;

    @Column(name = "activo", nullable = false)
    private Boolean active;
}
