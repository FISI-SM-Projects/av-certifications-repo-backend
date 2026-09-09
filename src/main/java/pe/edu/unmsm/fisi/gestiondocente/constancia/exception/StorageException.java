package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;

public class StorageException extends BaseDomainException {

    public StorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public StorageException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public StorageException(String message, HttpStatus status) {
        super(message, status);
    }

    public StorageException(String message, HttpStatus status, Throwable cause) {
        super(message, status, cause);
    }
}
