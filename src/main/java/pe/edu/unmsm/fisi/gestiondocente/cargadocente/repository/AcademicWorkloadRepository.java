package pe.edu.unmsm.fisi.gestiondocente.cargadocente.repository;

import pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity.AcademicWorkload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicWorkloadRepository extends JpaRepository<AcademicWorkload, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"teacher", "course", "academicPeriod"})
    java.util.List<AcademicWorkload> findByTeacherCodeOrderById(String code);
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"teacher", "course", "academicPeriod"})
    java.util.List<AcademicWorkload> findByTeacherCodeAndAcademicPeriodSemesterCodeOrderById(String code, String semesterCode);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select w from AcademicWorkload w where w.id = :id")
    java.util.Optional<AcademicWorkload> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);
}
