package pe.edu.unmsm.fisi.gestiondocente.exception.constancia;

public class TeacherNotFoundForCertificateException extends RuntimeException {

    public TeacherNotFoundForCertificateException(String teacherCode) {
        super("No se encontro el docente con codigo " + teacherCode);
    }
}
