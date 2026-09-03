package pe.edu.unmsm.fisi.gestiondocente.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_system_role")
@Getter
@Setter
@ToString(exclude = {"account", "systemRole"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSystemRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private InstitutionalAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_role_id", nullable = false)
    private SystemRole systemRole;

    @Column(name = "granted_by_account_id", nullable = false)
    private Long grantedByAccountId;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
