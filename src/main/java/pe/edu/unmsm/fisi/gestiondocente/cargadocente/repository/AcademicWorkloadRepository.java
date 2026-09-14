package pe.edu.unmsm.fisi.gestiondocente.cargadocente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity.AcademicWorkload;

public interface AcademicWorkloadRepository extends JpaRepository<AcademicWorkload, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"teacher", "course", "academicPeriod"})
    java.util.List<AcademicWorkload> findByTeacherCodeOrderById(String code);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"teacher", "course", "academicPeriod"})
    @Query(
            value = """
                    select w
                    from AcademicWorkload w
                    join w.teacher t
                    join w.course c
                    join w.academicPeriod p
                    where t.code = :teacherCode
                      and (:semesterFilter = false or lower(p.semesterCode) = :semester)
                      and (:cycle is null or w.cycle = :cycle)
                      and (:plan is null or w.plan = :plan)
                      and (
                          :courseFilter = false
                          or lower(c.code) like :coursePattern
                          or lower(c.name) like :coursePattern
                      )
                    """,
            countQuery = """
                    select count(w)
                    from AcademicWorkload w
                    join w.teacher t
                    join w.course c
                    join w.academicPeriod p
                    where t.code = :teacherCode
                      and (:semesterFilter = false or lower(p.semesterCode) = :semester)
                      and (:cycle is null or w.cycle = :cycle)
                      and (:plan is null or w.plan = :plan)
                      and (
                          :courseFilter = false
                          or lower(c.code) like :coursePattern
                          or lower(c.name) like :coursePattern
                      )
                    """
    )
    org.springframework.data.domain.Page<AcademicWorkload> findTeacherCourses(
            @Param("teacherCode") String teacherCode,
            @Param("semesterFilter") boolean semesterFilter,
            @Param("semester") String semester,
            @Param("cycle") Integer cycle,
            @Param("plan") Integer plan,
            @Param("courseFilter") boolean courseFilter,
            @Param("coursePattern") String coursePattern,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"teacher", "course", "academicPeriod"})
    java.util.List<AcademicWorkload> findByTeacherCodeAndAcademicPeriodSemesterCodeOrderById(String code, String semesterCode);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select w from AcademicWorkload w where w.id = :id")
    java.util.Optional<AcademicWorkload> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);
}
