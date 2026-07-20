package pe.edu.unmsm.fisi.gestiondocente.exception.constancia;

public class GenerationAlreadyExistsException extends StorageException {

    public GenerationAlreadyExistsException(String generationId) {
        super("La generacion ya existe: " + generationId);
    }
}
