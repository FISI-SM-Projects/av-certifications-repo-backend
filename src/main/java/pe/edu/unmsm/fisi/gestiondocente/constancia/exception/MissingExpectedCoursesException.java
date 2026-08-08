package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import java.util.List;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.ExpectedCourseRequest;

public class MissingExpectedCoursesException extends RuntimeException {

    private final List<ExpectedCourseRequest> missingCourses;

    public MissingExpectedCoursesException(List<ExpectedCourseRequest> missingCourses) {
        super("No se puede generar la constancia semestral porque faltan constancias por curso");
        this.missingCourses = List.copyOf(missingCourses);
    }

    public List<ExpectedCourseRequest> getMissingCourses() {
        return missingCourses;
    }
}
