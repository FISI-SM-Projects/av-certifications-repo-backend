package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import java.util.ArrayList;
import java.util.List;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.ExpectedCourseRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.SemesterCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingRequiredFieldsException;

public class SemesterCertificateRequestValidator {

    public void validate(SemesterCertificateRequest request) {
        List<String> missingFields = new ArrayList<>();

        if (request == null) {
            missingFields.add("teacher_code");
            missingFields.add("semester");
            missingFields.add("expected_courses");
            throwIfMissing(missingFields);
        }

        addIfBlank(request.getTeacherCode(), "teacher_code", missingFields);
        addIfBlank(request.getSemester(), "semester", missingFields);
        validateExpectedCourses(request.getExpectedCourses(), missingFields);
        throwIfMissing(missingFields);
    }

    private void validateExpectedCourses(List<ExpectedCourseRequest> expectedCourses, List<String> missingFields) {
        if (expectedCourses == null || expectedCourses.isEmpty()) {
            missingFields.add("expected_courses");
            return;
        }

        for (int i = 0; i < expectedCourses.size(); i++) {
            ExpectedCourseRequest expectedCourse = expectedCourses.get(i);

            if (expectedCourse == null) {
                missingFields.add("expected_courses[" + i + "]");
                continue;
            }

            addIfBlank(expectedCourse.getCode(), "expected_courses[" + i + "].code", missingFields);
            addIfBlank(expectedCourse.getSection(), "expected_courses[" + i + "].section", missingFields);
        }
    }

    private void addIfBlank(String value, String field, List<String> missingFields) {
        if (value == null || value.trim().isEmpty()) {
            missingFields.add(field);
        }
    }

    private void throwIfMissing(List<String> missingFields) {
        if (!missingFields.isEmpty()) {
            throw new MissingRequiredFieldsException(missingFields);
        }
    }
}
