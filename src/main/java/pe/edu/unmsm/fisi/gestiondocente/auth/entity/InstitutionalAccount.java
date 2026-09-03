package pe.edu.unmsm.fisi.gestiondocente.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Entity
@Table(name = "institutional_account")
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
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(name = "ldap_uid", nullable = false, unique = true, length = 100)
    private String ldapUid;

    @Column(name = "institutional_email", nullable = false, unique = true, length = 180)
    private String institutionalEmail;

    @Column(name = "main", nullable = false)
    private Boolean main;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "account_status", nullable = false, length = 30)
    private AccountStatus accountStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (person == null || person.getPersonSystemRoles() == null) {
            return Collections.emptyList();
        }

        return person.getPersonSystemRoles().stream()
                .filter(pr -> Boolean.TRUE.equals(pr.getActive())
                        && pr.getSystemRole() != null
                        && Boolean.TRUE.equals(pr.getSystemRole().getActive()))
                .map(pr -> new SimpleGrantedAuthority("ROLE_" + pr.getSystemRole().getCode().name()))
                .collect(Collectors.toSet());
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
        return (this.accountStatus != AccountStatus.SUSPENDIDO && this.accountStatus != AccountStatus.ELIMINADO);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.accountStatus == AccountStatus.ACTIVO &&
                (this.person != null && this.person.getRegisterState() == AccountStatus.ACTIVO);
    }
}
