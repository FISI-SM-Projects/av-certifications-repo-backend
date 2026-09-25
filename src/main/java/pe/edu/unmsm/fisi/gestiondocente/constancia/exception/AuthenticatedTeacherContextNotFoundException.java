package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;

import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;

public class AuthenticatedTeacherContextNotFoundException extends BaseDomainException {

    public AuthenticatedTeacherContextNotFoundException() {
        super("No se pudo resolver el docente autenticado", HttpStatus.NOT_FOUND);
    }
}
