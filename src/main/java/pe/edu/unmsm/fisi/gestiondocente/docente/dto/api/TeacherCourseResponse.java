package pe.edu.unmsm.fisi.gestiondocente.docente.dto.api;

import java.time.OffsetDateTime;

public record TeacherCourseResponse(
        Long id,
        Long moodleId,
        Long teacherId,
        Integer cycle,
        Integer section,
        Integer plan,
        String school,
        CourseSummary course,
        AcademicPeriodSummary academicPeriod
) {
    public record CourseSummary(Long id, String code, String name) {
    }

    public record AcademicPeriodSummary(Long id, String semesterCode, OffsetDateTime startDate, OffsetDateTime endDate) {
    }
}
