package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.ExpectedCourseRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.SemesterCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.DuplicateExpectedCoursesException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidStoragePathException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidRequestFieldsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidRequestFieldsException.InvalidField;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingRequiredFieldsException;

@Component
public class SemesterCertificateRequestValidator {

    private final StoragePathSanitizer storagePathSanitizer = new StoragePathSanitizer();

    public void validate(SemesterCertificateRequest request) {
        List<String> missingFields = new ArrayList<>();

        if (request == null) {
            missingFields.add("teacher_code");
            missingFields.add("semester");
            missingFields.add("expected_courses");
            throwIfMissing(missingFields);
            return;
        }

        addIfBlank(request.getTeacherCode(), "teacher_code", missingFields);
        addIfBlank(request.getSemester(), "semester", missingFields);
        validateExpectedCourses(request.getExpectedCourses(), missingFields);
        throwIfMissing(missingFields);
        validateDuplicates(request);
        validateFormats(request);
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

    private void validateFormats(SemesterCertificateRequest request) {
        List<InvalidField> invalidFields = new ArrayList<>();
        validateLength(request.getTeacherCode(), "teacher_code",
                CertificateRequestValidationRules.TEACHER_CODE_MIN,
                CertificateRequestValidationRules.TEACHER_CODE_MAX, invalidFields);
        validatePathSegment(request.getTeacherCode(), "teacher_code", invalidFields);
        validateLength(request.getSemester(), "semester",
                CertificateRequestValidationRules.SEMESTER_MIN,
                CertificateRequestValidationRules.SEMESTER_MAX, invalidFields);
        validatePathSegment(request.getSemester(), "semester", invalidFields);

        if (request.getExpectedCourses().size() > CertificateRequestValidationRules.EXPECTED_COURSES_MAX) {
            invalidFields.add(new InvalidField("expected_courses",
                    "Debe tener como maximo " + CertificateRequestValidationRules.EXPECTED_COURSES_MAX
                            + " elementos"));
        }

        for (int i = 0; i < request.getExpectedCourses().size(); i++) {
            ExpectedCourseRequest expectedCourse = request.getExpectedCourses().get(i);
            validateLength(expectedCourse.getCode(), "expected_courses[" + i + "].code",
                    CertificateRequestValidationRules.COURSE_CODE_MIN,
                    CertificateRequestValidationRules.COURSE_CODE_MAX, invalidFields);
            validatePathSegment(expectedCourse.getCode(), "expected_courses[" + i + "].code", invalidFields);
            validateLength(expectedCourse.getSection(), "expected_courses[" + i + "].section",
                    CertificateRequestValidationRules.SECTION_MIN,
                    CertificateRequestValidationRules.SECTION_MAX, invalidFields);
            validatePathSegment(expectedCourse.getSection(), "expected_courses[" + i + "].section", invalidFields);
        }

        if (!invalidFields.isEmpty()) {
            throw new InvalidRequestFieldsException(invalidFields);
        }
    }

    private void validateDuplicates(SemesterCertificateRequest request) {
        Map<String, ExpectedCourseRequest> seenCourses = new LinkedHashMap<>();
        List<ExpectedCourseRequest> duplicateCourses = new ArrayList<>();

        for (ExpectedCourseRequest expectedCourse : request.getExpectedCourses()) {
            String normalizedCode = CertificateRequestNormalizer.code(expectedCourse.getCode());
            String normalizedSection = CertificateRequestNormalizer.code(expectedCourse.getSection());
            String key = normalizedCode + "::" + normalizedSection;
            if (seenCourses.containsKey(key)) {
                duplicateCourses.add(new ExpectedCourseRequest(normalizedCode, normalizedSection));
            } else {
                seenCourses.put(key, expectedCourse);
            }
        }

        if (!duplicateCourses.isEmpty()) {
            throw new DuplicateExpectedCoursesException(duplicateCourses);
        }
    }

    private void validateLength(String value, String field, int min, int max, List<InvalidField> invalidFields) {
        int length = value.length();
        if (length < min || length > max) {
            invalidFields.add(new InvalidField(field, "Debe tener entre " + min + " y " + max + " caracteres"));
        }
    }

    private void validatePathSegment(String value, String field, List<InvalidField> invalidFields) {
        try {
            storagePathSanitizer.sanitizeSegment(value);
        } catch (InvalidStoragePathException exception) {
            invalidFields.add(new InvalidField(field, "Contiene caracteres no permitidos para identificadores"));
        }
    }
}
