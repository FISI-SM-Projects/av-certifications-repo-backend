package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;

public class TeacherIdentityMismatchException extends BaseDomainException {

    public TeacherIdentityMismatchException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
