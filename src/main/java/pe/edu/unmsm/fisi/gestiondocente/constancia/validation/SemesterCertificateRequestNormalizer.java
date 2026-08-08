package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.ExpectedCourseRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.SemesterCertificateRequest;

@Component
public class SemesterCertificateRequestNormalizer {

    public SemesterCertificateRequest normalize(SemesterCertificateRequest request) {
        if (request == null) {
            return null;
        }

        List<ExpectedCourseRequest> expectedCourses = request.getExpectedCourses() == null
                ? null
                : request.getExpectedCourses().stream()
                        .map(this::normalizeExpectedCourse)
                        .toList();

        return new SemesterCertificateRequest(
                CertificateRequestNormalizer.code(request.getTeacherCode()),
                CertificateRequestNormalizer.code(request.getSemester()),
                expectedCourses);
    }

    private ExpectedCourseRequest normalizeExpectedCourse(ExpectedCourseRequest expectedCourse) {
        if (expectedCourse == null) {
            return null;
        }

        return new ExpectedCourseRequest(
                CertificateRequestNormalizer.code(expectedCourse.getCode()),
                CertificateRequestNormalizer.code(expectedCourse.getSection()));
    }
}
