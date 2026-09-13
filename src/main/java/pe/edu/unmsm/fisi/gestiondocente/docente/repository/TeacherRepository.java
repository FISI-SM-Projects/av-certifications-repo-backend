package pe.edu.unmsm.fisi.gestiondocente.docente.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    java.util.Optional<Teacher> findByCode(String code);
    java.util.Optional<Teacher> findByPersonId(Long personId);

    @EntityGraph(attributePaths = "person")
    List<Teacher> findAllByOrderByCodeAsc();

    @EntityGraph(attributePaths = "person")
    List<Teacher> findByDepartmentOrderByCodeAsc(Department department);
}
