package pe.edu.unmsm.fisi.gestiondocente.teacher.dto;

import pe.edu.unmsm.fisi.gestiondocente.auth.entity.AccountStatus;
import pe.edu.unmsm.fisi.gestiondocente.teacher.entitiy.Department;

public record TeacherProfile(
        Long id,
        Long personId,
        Long moodleId,
        String code,
        String dni,
        String firstName,
        String paternalLastName,
        String maternalLastName,
        Department department,
        AccountStatus registerState
) {
}
