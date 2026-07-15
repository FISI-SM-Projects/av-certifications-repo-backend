package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CoursePayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.IssuerPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.TeacherPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingRequiredFieldsException;

@Component
public class CourseCertificateRequestValidator {

    public void validate(CourseCertificateRequest request) {
        List<String> missingFields = new ArrayList<>();

        if (request == null) {
            missingFields.add("teacher");
            missingFields.add("course");
            missingFields.add("issuer");
            throwIfMissing(missingFields);
        }

        validateTeacher(request.getTeacher(), missingFields);
        validateCourse(request.getCourse(), missingFields);
        validateIssuer(request.getIssuer(), missingFields);
        throwIfMissing(missingFields);
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
}
