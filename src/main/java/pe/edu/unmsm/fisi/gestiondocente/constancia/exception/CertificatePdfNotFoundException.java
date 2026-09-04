package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;

public class CertificatePdfNotFoundException extends BaseDomainException {

    public CertificatePdfNotFoundException(String generationId) {
        super("PDF de constancia no encontrado", HttpStatus.NOT_FOUND);
    }
}
