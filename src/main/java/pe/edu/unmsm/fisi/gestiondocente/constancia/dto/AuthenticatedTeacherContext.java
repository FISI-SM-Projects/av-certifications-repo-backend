package pe.edu.unmsm.fisi.gestiondocente.constancia.dto;

public record AuthenticatedTeacherContext(
        Long teacherId,
        String teacherCode,
        String username,
        String institutionalEmail,
        String firstName,
        String paternalLastName,
        String maternalLastName,
        String department) {

    public String fullName() {
        String maternal = maternalLastName == null || maternalLastName.isBlank()
                ? ""
                : " " + maternalLastName;
        return firstName + " " + paternalLastName + maternal;
    }
}
