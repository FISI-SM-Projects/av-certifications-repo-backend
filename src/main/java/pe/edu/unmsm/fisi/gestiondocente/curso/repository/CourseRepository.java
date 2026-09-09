package pe.edu.unmsm.fisi.gestiondocente.curso.repository;

import pe.edu.unmsm.fisi.gestiondocente.curso.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

}
