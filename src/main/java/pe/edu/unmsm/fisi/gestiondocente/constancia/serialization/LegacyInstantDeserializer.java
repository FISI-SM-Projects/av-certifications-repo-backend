package pe.edu.unmsm.fisi.gestiondocente.constancia.serialization;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LegacyInstantDeserializer extends JsonDeserializer<Instant> {

    private static final ZoneId LEGACY_ZONE = ZoneId.of("America/Lima");

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalizedValue = value.trim();
        try {
            return Instant.parse(normalizedValue);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(normalizedValue).atZone(LEGACY_ZONE).toInstant();
            } catch (DateTimeParseException exception) {
                throw context.weirdStringException(value, Instant.class,
                        "Debe ser un instante ISO-8601 o una fecha legacy sin zona");
            }
        }
    }
}
