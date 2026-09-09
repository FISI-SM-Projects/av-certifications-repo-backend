package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorDetails;

import java.util.List;

public class InvalidRequestFieldsException extends BaseDomainException {

    public static final String DEFAULT_MESSAGE = "La solicitud contiene campos invalidos";

    private final List<InvalidField> invalidFields;

    public InvalidRequestFieldsException(List<InvalidField> invalidFields) {
        super(DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST, mapDetails(invalidFields));
        this.invalidFields = List.copyOf(invalidFields);
    }

    public List<InvalidField> getInvalidFields() {
        return invalidFields;
    }

    private static ErrorDetails[] mapDetails(List<InvalidField> invalidFields) {
        if (invalidFields == null) {
            return new ErrorDetails[0];
        }
        return invalidFields.stream()
                .map(field -> new ErrorDetails(field.field(), field.reason()))
                .toArray(ErrorDetails[]::new);
    }

    public record InvalidField(String field, String reason) {
    }
}
