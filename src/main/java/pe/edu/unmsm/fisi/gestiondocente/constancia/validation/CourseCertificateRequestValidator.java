package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CoursePayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.IssuerPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.TeacherPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidStoragePathException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidRequestFieldsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidRequestFieldsException.InvalidField;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingRequiredFieldsException;

@Component
public class CourseCertificateRequestValidator {

    private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final StoragePathSanitizer storagePathSanitizer = new StoragePathSanitizer();

    public void validate(CourseCertificateRequest request) {
        List<String> missingFields = new ArrayList<>();

        if (request == null) {
            missingFields.add("teacher");
            missingFields.add("course");
            missingFields.add("issuer");
            throwIfMissing(missingFields);
            return;
        }

        validateTeacher(request.getTeacher(), missingFields);
        validateCourse(request.getCourse(), missingFields);
        validateIssuer(request.getIssuer(), missingFields);
        throwIfMissing(missingFields);
        validateFormats(request);
    }

    private void validateTeacher(TeacherPayload teacher, List<String> missingFields) {
        if (teacher == null) {
            missingFields.add("teacher");
            return;
        }

        addIfBlank(teacher.getFullName(), "teacher.full_name", missingFields);
        addIfBlank(teacher.getEmail(), "teacher.email", missingFields);
        addIfBlank(teacher.getTeacherCode(), "teacher.teacher_code", missingFields);
    }

    private void validateCourse(CoursePayload course, List<String> missingFields) {
        if (course == null) {
            missingFields.add("course");
            return;
        }

        addIfBlank(course.getCode(), "course.code", missingFields);
        addIfBlank(course.getSubject(), "course.subject", missingFields);
        addIfBlank(course.getCycle(), "course.cycle", missingFields);
        addIfBlank(course.getSection(), "course.section", missingFields);
        addIfBlank(course.getSchool(), "course.school", missingFields);
        addIfBlank(course.getPlan(), "course.plan", missingFields);
        addIfBlank(course.getSemester(), "course.semester", missingFields);
    }

    private void validateIssuer(IssuerPayload issuer, List<String> missingFields) {
        if (issuer == null) {
            missingFields.add("issuer");
            return;
        }

        addIfBlank(issuer.getSystem(), "issuer.system", missingFields);
        addIfBlank(issuer.getExecutedByUserid(), "issuer.executed_by_userid", missingFields);
        addIfBlank(issuer.getExecutedByEmail(), "issuer.executed_by_email", missingFields);
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

    private void validateFormats(CourseCertificateRequest request) {
        List<InvalidField> invalidFields = new ArrayList<>();
        TeacherPayload teacher = request.getTeacher();
        CoursePayload course = request.getCourse();
        IssuerPayload issuer = request.getIssuer();

        validateLength(teacher.getFullName(), "teacher.full_name",
                CertificateRequestValidationRules.FULL_NAME_MIN,
                CertificateRequestValidationRules.FULL_NAME_MAX, invalidFields);
        validateEmail(teacher.getEmail(), "teacher.email", invalidFields);
        validateLength(teacher.getTeacherCode(), "teacher.teacher_code",
                CertificateRequestValidationRules.TEACHER_CODE_MIN,
                CertificateRequestValidationRules.TEACHER_CODE_MAX, invalidFields);
        validatePathSegment(teacher.getTeacherCode(), "teacher.teacher_code", invalidFields);

        validateLength(course.getCode(), "course.code",
                CertificateRequestValidationRules.COURSE_CODE_MIN,
                CertificateRequestValidationRules.COURSE_CODE_MAX, invalidFields);
        validatePathSegment(course.getCode(), "course.code", invalidFields);
        validateLength(course.getSubject(), "course.subject",
                CertificateRequestValidationRules.SUBJECT_MIN,
                CertificateRequestValidationRules.SUBJECT_MAX, invalidFields);
        validateLength(course.getCycle(), "course.cycle",
                CertificateRequestValidationRules.CYCLE_MIN,
                CertificateRequestValidationRules.CYCLE_MAX, invalidFields);
        validateLength(course.getSection(), "course.section",
                CertificateRequestValidationRules.SECTION_MIN,
                CertificateRequestValidationRules.SECTION_MAX, invalidFields);
        validatePathSegment(course.getSection(), "course.section", invalidFields);
        validateLength(course.getSchool(), "course.school",
                CertificateRequestValidationRules.SCHOOL_MIN,
                CertificateRequestValidationRules.SCHOOL_MAX, invalidFields);
        validateLength(course.getPlan(), "course.plan",
                CertificateRequestValidationRules.PLAN_MIN,
                CertificateRequestValidationRules.PLAN_MAX, invalidFields);
        validateLength(course.getSemester(), "course.semester",
                CertificateRequestValidationRules.SEMESTER_MIN,
                CertificateRequestValidationRules.SEMESTER_MAX, invalidFields);
        validatePathSegment(course.getSemester(), "course.semester", invalidFields);

        validateLength(issuer.getSystem(), "issuer.system",
                CertificateRequestValidationRules.ISSUER_SYSTEM_MIN,
                CertificateRequestValidationRules.ISSUER_SYSTEM_MAX, invalidFields);
        validateLength(issuer.getExecutedByUserid(), "issuer.executed_by_userid",
                CertificateRequestValidationRules.EXECUTED_BY_USER_ID_MIN,
                CertificateRequestValidationRules.EXECUTED_BY_USER_ID_MAX, invalidFields);
        validateEmail(issuer.getExecutedByEmail(), "issuer.executed_by_email", invalidFields);

        if (!invalidFields.isEmpty()) {
            throw new InvalidRequestFieldsException(invalidFields);
        }
    }

    private void validateEmail(String value, String field, List<InvalidField> invalidFields) {
        validateMaxLength(value, field, CertificateRequestValidationRules.EMAIL_MAX, invalidFields);
        if (!SIMPLE_EMAIL_PATTERN.matcher(value).matches()) {
            invalidFields.add(new InvalidField(field, "Debe tener un formato de correo valido"));
        }
    }

    private void validateLength(String value, String field, int min, int max, List<InvalidField> invalidFields) {
        int length = value.length();
        if (length < min || length > max) {
            invalidFields.add(new InvalidField(field, "Debe tener entre " + min + " y " + max + " caracteres"));
        }
    }

    private void validateMaxLength(String value, String field, int max, List<InvalidField> invalidFields) {
        if (value.length() > max) {
            invalidFields.add(new InvalidField(field, "Debe tener como maximo " + max + " caracteres"));
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
