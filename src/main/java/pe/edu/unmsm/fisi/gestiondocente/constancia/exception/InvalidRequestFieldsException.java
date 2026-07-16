package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import java.util.List;

public class InvalidRequestFieldsException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "La solicitud contiene campos invalidos";

    private final List<InvalidField> invalidFields;

    public InvalidRequestFieldsException(List<InvalidField> invalidFields) {
        super(DEFAULT_MESSAGE);
        this.invalidFields = List.copyOf(invalidFields);
    }

    public List<InvalidField> getInvalidFields() {
        return invalidFields;
    }

    public record InvalidField(String field, String reason) {
    }
}
