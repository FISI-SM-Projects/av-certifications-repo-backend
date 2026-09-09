package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.ExpectedCourseRequest;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorDetails;

import java.util.List;

public class MissingExpectedCoursesException extends BaseDomainException {

    private final List<ExpectedCourseRequest> missingCourses;

    public MissingExpectedCoursesException(List<ExpectedCourseRequest> missingCourses) {
        super("No se puede generar la constancia semestral porque faltan constancias por curso", HttpStatus.CONFLICT, mapDetails(missingCourses));
        this.missingCourses = List.copyOf(missingCourses);
    }

    public List<ExpectedCourseRequest> getMissingCourses() {
        return missingCourses;
    }

    private static ErrorDetails[] mapDetails(List<ExpectedCourseRequest> missingCourses) {
        if (missingCourses == null) {
            return new ErrorDetails[0];
        }
        return missingCourses.stream()
                .map(c -> new ErrorDetails(c.getCode() != null ? c.getCode() : "course", "Curso faltante"))
                .toArray(ErrorDetails[]::new);
    }
}
