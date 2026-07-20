package pe.edu.unmsm.fisi.gestiondocente.exception.constancia;

public class ApprovedCertificateAlreadyExistsException extends RuntimeException {

    public ApprovedCertificateAlreadyExistsException() {
        super("La constancia ya fue aprobada y no admite nuevas generaciones");
    }
}
