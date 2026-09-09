package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;

public class ApprovedCertificateAlreadyExistsException extends BaseDomainException {

    public ApprovedCertificateAlreadyExistsException() {
        super("La constancia ya fue aprobada y no admite nuevas generaciones", HttpStatus.CONFLICT);
    }

    public ApprovedCertificateAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
