package pe.edu.unmsm.fisi.gestiondocente.teacher.dto;

import pe.edu.unmsm.fisi.gestiondocente.academic.dto.AcademicWorkloadDTO;

import java.util.List;

public record TeacherCourse(
        Long id,
        Long moodleId,
        String code,
        String fullName,
        List<AcademicWorkloadDTO> courses
) {
}
