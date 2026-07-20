package pe.edu.unmsm.fisi.gestiondocente.exception.constancia;

import java.util.List;

public class MissingRequiredFieldsException extends RuntimeException {

    public static final String DEFAULT_MESSAGE =
            "No se pudo procesar la solicitud porque faltan datos obligatorios";

    private final List<String> missingFields;

    public MissingRequiredFieldsException(List<String> missingFields) {
        super(DEFAULT_MESSAGE);
        this.missingFields = List.copyOf(missingFields);
    }

    public List<String> getMissingFields() {
        return missingFields;
    }
}
