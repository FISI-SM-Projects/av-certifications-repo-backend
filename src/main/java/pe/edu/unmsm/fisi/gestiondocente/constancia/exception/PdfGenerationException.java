package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;

public class PdfGenerationException extends BaseDomainException {

    public PdfGenerationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public PdfGenerationException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
