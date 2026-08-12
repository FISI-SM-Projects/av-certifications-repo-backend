package pe.edu.unmsm.fisi.gestiondocente.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.InstitutionalAccount;

import java.util.Optional;

@Repository
public interface InstitutionalAccountRepository extends JpaRepository<InstitutionalAccount, Long> {

    @Query("SELECT DISTINCT a FROM InstitutionalAccount a " +
           "JOIN FETCH a.person p " +
           "LEFT JOIN FETCH p.personRoles pr " +
           "LEFT JOIN FETCH pr.role r " +
           "WHERE a.ldapUid = :ldapUid")
    Optional<InstitutionalAccount> findByLdapUid(@Param("ldapUid") String ldapUid);
}
