package pe.edu.unmsm.fisi.gestiondocente.auth.dto;

import java.util.List;
public record CurrentUserResponse(Long accountId, Long personId, String ldapUid,
        String institutionalEmail, String fullName, List<String> roles, String accountStatus,
        String personStatus, TeacherContext teacher, Object student, Object administrative) {
    public record TeacherContext(Long teacherId, String teacherCode, Long moodleId, String department) {}
}
