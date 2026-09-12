package pe.edu.unmsm.fisi.gestiondocente.docente.dto.api;

public record TeacherMeResponse(
        Long id,
        Long personId,
        Long moodleId,
        String code,
        String dni,
        String email,
        String firstName,
        String paternalLastName,
        String maternalLastName,
        String department,
        String registerState
) {
}
