package pe.edu.unmsm.fisi.gestiondocente.academic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.unmsm.fisi.gestiondocente.academic.dto.AcademicWorkloadDTO;
import pe.edu.unmsm.fisi.gestiondocente.academic.entity.AcademicWorkload;

@Repository
public interface AcademicWorkloadRepository extends JpaRepository<AcademicWorkload, Long> {

    @Query(
            value = """
                SELECT new pe.edu.unmsm.fisi.gestiondocente.academic.dto.AcademicWorkloadDTO(
                    aw.id,
                    aw.moodleId,
                    t.id as teacherId,
                    aw.cycle,
                    aw.section,
                    aw.plan,
                    aw.school,
                    new pe.edu.unmsm.fisi.gestiondocente.academic.dto.CourseDTO(
                        c.id,
                        c.code,
                        c.name
                    ),
                    new pe.edu.unmsm.fisi.gestiondocente.academic.dto.AcademicPeriodDTO(
                        ap.id,
                        ap.semesterCode,
                        ap.startDate,
                        ap.endDate
                    )
                )
                FROM AcademicWorkload aw
                JOIN aw.course c
                JOIN aw.academicPeriod ap
                JOIN aw.teacher t
                WHERE t.person.id = :personId
                AND (:cycle IS NULL OR aw.cycle = :cycle)
                AND (:plan IS NULL OR aw.plan = :plan)
                AND (:semester IS NULL OR ap.semesterCode = :semester)
                AND (CAST(:course AS string) IS NULL 
                     OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:course AS string), '%')) 
                     OR LOWER(c.code) LIKE LOWER(CONCAT('%', CAST(:course AS string), '%')))
                """,
                countQuery = """
                SELECT COUNT(aw)
                FROM AcademicWorkload aw
                JOIN aw.teacher t
                JOIN aw.course c
                JOIN aw.academicPeriod ap
                WHERE t.person.id = :personId
                    AND (:cycle IS NULL OR aw.cycle = :cycle)
                    AND (:plan IS NULL OR aw.plan = :plan)
                    AND (:semester IS NULL OR ap.semesterCode = :semester)
                    AND (CAST(:course AS string) IS NULL 
                     OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:course AS string), '%')) 
                     OR LOWER(c.code) LIKE LOWER(CONCAT('%', CAST(:course AS string), '%')))
                """
    )
    Page<AcademicWorkloadDTO> findCoursesByTeacherPersonId(
        @Param("personId") Long personId,
        @Param("cycle") Integer cycle,
        @Param("plan") Integer plan,
        @Param("semester") String semester, 
        @Param("course") String course,
        Pageable pageable);
}
