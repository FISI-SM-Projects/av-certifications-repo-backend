package pe.edu.unmsm.fisi.gestiondocente.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "cuenta_institucional", schema = "auth")
@Getter
@Setter
@ToString(exclude = "person")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionalAccount implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "persona_id", nullable = false)
    private Person person;

    @Column(name = "uid_ldap", nullable = false, unique = true, length = 100)
    private String ldapUid;

    @Column(name = "correo_institucional", nullable = false, unique = true, length = 180)
    private String institutionalEmail;

    @Column(name = "dn_ldap", length = 500)
    private String ldapDn;

    @Column(name = "es_principal", nullable = false)
    private Boolean isPrincipal;

    @Column(name = "estado_cuenta", nullable = false, length = 30)
    private String accountStatus;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime updatedAt;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (person == null || person.getPersonRoles() == null) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();

        return person.getPersonRoles().stream()
                .filter(pr -> pr.getRole() != null && Boolean.TRUE.equals(pr.getRole().getActive()))
                .filter(pr -> pr.getStartDate() != null && !pr.getStartDate().isAfter(today))
                .filter(pr -> pr.getEndDate() == null || !pr.getEndDate().isBefore(today))
                .map(pr -> new SimpleGrantedAuthority(pr.getRole().getCode()))
                .toList();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.ldapUid;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"BLOQUEADA".equalsIgnoreCase(this.accountStatus);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVA".equalsIgnoreCase(this.accountStatus) &&
                (this.person != null && "ACTIVA".equalsIgnoreCase(this.person.getRegisterState()));
    }
}
