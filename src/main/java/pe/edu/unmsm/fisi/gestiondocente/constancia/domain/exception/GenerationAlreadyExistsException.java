package pe.edu.unmsm.fisi.gestiondocente.constancia.domain.exception;

public class GenerationAlreadyExistsException extends StorageException {

    public GenerationAlreadyExistsException(String generationId) {
        super("La generacion ya existe: " + generationId);
    }
}
