package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;

public class InvalidStoragePathException extends StorageException {

    public InvalidStoragePathException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
