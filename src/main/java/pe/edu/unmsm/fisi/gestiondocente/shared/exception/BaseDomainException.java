package pe.edu.unmsm.fisi.gestiondocente.shared.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorDetails;

public abstract class BaseDomainException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorDetails[] details;

    public BaseDomainException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.details = new ErrorDetails[0];
    }

    public BaseDomainException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.details = new ErrorDetails[0];
    }

    public BaseDomainException(String message, HttpStatus status, ErrorDetails[] details) {
        super(message);
        this.status = status;
        this.details = details != null ? details : new ErrorDetails[0];
    }

    public BaseDomainException(String message, HttpStatus status, ErrorDetails[] details, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.details = details != null ? details : new ErrorDetails[0];
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorDetails[] getDetails() {
        return details;
    }
}
