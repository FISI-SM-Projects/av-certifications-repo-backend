package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

public class CertificateGenerationNotFoundException extends RuntimeException {

    public CertificateGenerationNotFoundException(String generationId) {
        super("Generación de constancia no encontrada");
    }
}
