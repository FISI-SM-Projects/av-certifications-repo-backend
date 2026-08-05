package pe.edu.unmsm.fisi.gestiondocente.exception.constancia;

public class CertificatePdfNotFoundException extends RuntimeException {

    public CertificatePdfNotFoundException(String generationId) {
        super("PDF de constancia no encontrado");
    }
}
