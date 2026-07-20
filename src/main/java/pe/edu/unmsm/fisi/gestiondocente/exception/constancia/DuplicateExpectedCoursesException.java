package pe.edu.unmsm.fisi.gestiondocente.exception.constancia;

import java.util.List;

import pe.edu.unmsm.fisi.gestiondocente.dto.constancia.request.ExpectedCourseRequest;

public class DuplicateExpectedCoursesException extends RuntimeException {

    private final List<ExpectedCourseRequest> duplicateCourses;

    public DuplicateExpectedCoursesException(List<ExpectedCourseRequest> duplicateCourses) {
        super("La solicitud contiene cursos duplicados");
        this.duplicateCourses = List.copyOf(duplicateCourses);
    }

    public List<ExpectedCourseRequest> getDuplicateCourses() {
        return duplicateCourses;
    }
}
