package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;

public class CertificateSourceInconsistencyException extends BaseDomainException {

    public CertificateSourceInconsistencyException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
