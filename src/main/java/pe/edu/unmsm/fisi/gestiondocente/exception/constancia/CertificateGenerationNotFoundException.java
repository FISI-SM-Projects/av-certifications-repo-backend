package pe.edu.unmsm.fisi.gestiondocente.exception.constancia;

public class CertificateGenerationNotFoundException extends RuntimeException {

    public CertificateGenerationNotFoundException(String generationId) {
        super("Generación de constancia no encontrada");
    }
}
