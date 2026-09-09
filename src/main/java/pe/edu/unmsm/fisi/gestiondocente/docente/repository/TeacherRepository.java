package pe.edu.unmsm.fisi.gestiondocente.docente.repository;

import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    java.util.Optional<Teacher> findByCode(String code);
    java.util.Optional<Teacher> findByPersonId(Long personId);
}
