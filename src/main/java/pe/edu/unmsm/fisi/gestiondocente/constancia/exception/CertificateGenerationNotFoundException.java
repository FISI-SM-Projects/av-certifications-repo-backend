package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;

public class CertificateGenerationNotFoundException extends BaseDomainException {

    public CertificateGenerationNotFoundException(String generationId) {
        super("Generación de constancia no encontrada", HttpStatus.NOT_FOUND);
    }
}
