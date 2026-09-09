package pe.edu.unmsm.fisi.gestiondocente.docente.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;

public class DocenteNotFoundException extends BaseDomainException {

    public DocenteNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
