package pe.edu.unmsm.fisi.gestiondocente.auth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorResponse;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorResponseFactory;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        String jwtError = (String) request.getAttribute("jwt_error");
        String message = (jwtError != null) ? jwtError : "No autorizado: Se requiere un token JWT válido";

        ErrorResponse errorResponse = ErrorResponseFactory.create(
                HttpStatus.UNAUTHORIZED,
                message,
                request
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
