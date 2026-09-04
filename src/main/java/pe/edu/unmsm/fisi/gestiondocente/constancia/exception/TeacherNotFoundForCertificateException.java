package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;

public class TeacherNotFoundForCertificateException extends BaseDomainException {

    public TeacherNotFoundForCertificateException(String teacherCode) {
        super("No se encontro el docente con codigo " + teacherCode, HttpStatus.NOT_FOUND);
    }
}
