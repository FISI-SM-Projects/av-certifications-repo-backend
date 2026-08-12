package pe.edu.unmsm.fisi.gestiondocente.person.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;

@Repository
public interface PersonRepository extends JpaRepository <Person, Long> {

}
