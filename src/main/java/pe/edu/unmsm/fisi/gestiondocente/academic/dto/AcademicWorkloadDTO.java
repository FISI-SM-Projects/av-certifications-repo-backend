package pe.edu.unmsm.fisi.gestiondocente.academic.dto;

import pe.edu.unmsm.fisi.gestiondocente.academic.entity.School;

public record AcademicWorkloadDTO(
        Long id,
        Long moodleId,
        Long teacherId,
        Integer cycle,
        Integer section,
        Integer plan,
        School school,
        CourseDTO course,
        AcademicPeriodDTO academicPeriod
) {
}
