package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

public class CertificatePdfNotFoundException extends RuntimeException {

    public CertificatePdfNotFoundException(String generationId) {
        super("PDF de constancia no encontrado");
    }
}
