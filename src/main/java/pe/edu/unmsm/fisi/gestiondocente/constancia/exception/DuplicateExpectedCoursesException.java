package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.ExpectedCourseRequest;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorDetails;

import java.util.List;

public class DuplicateExpectedCoursesException extends BaseDomainException {

    private final List<ExpectedCourseRequest> duplicateCourses;

    public DuplicateExpectedCoursesException(List<ExpectedCourseRequest> duplicateCourses) {
        super("La solicitud contiene cursos duplicados", HttpStatus.BAD_REQUEST, mapDetails(duplicateCourses));
        this.duplicateCourses = List.copyOf(duplicateCourses);
    }

    public List<ExpectedCourseRequest> getDuplicateCourses() {
        return duplicateCourses;
    }

    private static ErrorDetails[] mapDetails(List<ExpectedCourseRequest> duplicateCourses) {
        if (duplicateCourses == null) {
            return new ErrorDetails[0];
        }
        return duplicateCourses.stream()
                .map(c -> new ErrorDetails(c.getCode() != null ? c.getCode() : "course", "Curso duplicado"))
                .toArray(ErrorDetails[]::new);
    }
}
