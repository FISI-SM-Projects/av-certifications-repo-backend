package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorDetails;

import java.util.List;

public class MissingRequiredFieldsException extends BaseDomainException {

    public static final String DEFAULT_MESSAGE =
            "No se pudo procesar la solicitud porque faltan datos obligatorios";

    private final List<String> missingFields;

    public MissingRequiredFieldsException(List<String> missingFields) {
        super(DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST, mapDetails(missingFields));
        this.missingFields = List.copyOf(missingFields);
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    private static ErrorDetails[] mapDetails(List<String> missingFields) {
        if (missingFields == null) {
            return new ErrorDetails[0];
        }
        return missingFields.stream()
                .map(field -> new ErrorDetails(field, "Campo requerido obligatoriamente"))
                .toArray(ErrorDetails[]::new);
    }
}
