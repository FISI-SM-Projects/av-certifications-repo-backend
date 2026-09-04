package pe.edu.unmsm.fisi.gestiondocente.teacher.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.unmsm.fisi.gestiondocente.teacher.entitiy.Teacher;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    @EntityGraph(attributePaths = {"person"})
    Optional<Teacher> findByPersonId(Long personId);
}
