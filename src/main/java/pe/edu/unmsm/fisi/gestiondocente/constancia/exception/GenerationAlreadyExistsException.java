package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;

public class GenerationAlreadyExistsException extends StorageException {

    public GenerationAlreadyExistsException(String generationId) {
        super("La generación ya existe: " + generationId, HttpStatus.CONFLICT);
    }
}
