package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import java.util.List;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.ExpectedCourseRequest;

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
