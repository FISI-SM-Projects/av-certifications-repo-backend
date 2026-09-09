package pe.edu.unmsm.fisi.gestiondocente.constancia.repository;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"academicWorkload.teacher.person", "academicWorkload.course", "academicWorkload.academicPeriod"})
    java.util.List<Certification> findByAcademicWorkloadTeacherCodeOrderByIdDesc(String code);
    java.util.List<Certification> findByAcademicWorkloadIdOrderById(Long id);
}
