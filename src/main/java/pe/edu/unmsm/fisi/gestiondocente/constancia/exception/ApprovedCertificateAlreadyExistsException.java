package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

public class ApprovedCertificateAlreadyExistsException extends RuntimeException {

    public ApprovedCertificateAlreadyExistsException() {
        super("La constancia ya fue aprobada y no admite nuevas generaciones");
    }
}
