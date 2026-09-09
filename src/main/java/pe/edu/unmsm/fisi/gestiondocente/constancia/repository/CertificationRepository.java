package pe.edu.unmsm.fisi.gestiondocente.constancia.repository;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"academicWorkload.teacher.person", "academicWorkload.course", "academicWorkload.academicPeriod", "teacher.person", "academicPeriod"})
    java.util.Optional<Certification> findById(Long id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"academicWorkload.teacher.person", "academicWorkload.course", "academicWorkload.academicPeriod", "teacher.person", "academicPeriod"})
    java.util.List<Certification> findByTeacherCodeOrderByIdDesc(String code);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"academicWorkload.teacher.person", "academicWorkload.course", "academicWorkload.academicPeriod", "teacher.person", "academicPeriod"})
    java.util.List<Certification> findByTeacherCodeAndAcademicPeriodSemesterCodeOrderById(String code, String semesterCode);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"academicWorkload.teacher.person", "academicWorkload.course", "academicWorkload.academicPeriod", "teacher.person", "academicPeriod"})
    java.util.List<Certification> findByTeacherCodeAndAcademicPeriodSemesterCodeAndCertificateTypeOrderById(
            String code, String semesterCode, pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificationType certificateType);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"academicWorkload.teacher.person", "academicWorkload.course", "academicWorkload.academicPeriod", "teacher.person", "academicPeriod"})
    java.util.List<Certification> findByAcademicWorkloadIdOrderById(Long id);

    java.util.List<Certification> findByTeacherIdAndAcademicPeriodIdAndCertificateTypeOrderById(
            Long teacherId, Long academicPeriodId, pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificationType certificateType);
}
