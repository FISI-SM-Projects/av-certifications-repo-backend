package pe.edu.unmsm.fisi.gestiondocente.validation.constancia;

import org.springframework.stereotype.Component;

import pe.edu.unmsm.fisi.gestiondocente.dto.constancia.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.dto.constancia.request.CoursePayload;
import pe.edu.unmsm.fisi.gestiondocente.dto.constancia.request.IssuerPayload;
import pe.edu.unmsm.fisi.gestiondocente.dto.constancia.request.TeacherPayload;

@Component
public class CourseCertificateRequestNormalizer {

    public CourseCertificateRequest normalize(CourseCertificateRequest request) {
        if (request == null) {
            return null;
        }

        return new CourseCertificateRequest(
                normalizeTeacher(request.getTeacher()),
                normalizeCourse(request.getCourse()),
                normalizeIssuer(request.getIssuer()));
    }

    private TeacherPayload normalizeTeacher(TeacherPayload teacher) {
        if (teacher == null) {
            return null;
        }

        return new TeacherPayload(
                CertificateRequestNormalizer.text(teacher.getFullName()),
                CertificateRequestNormalizer.email(teacher.getEmail()),
                CertificateRequestNormalizer.code(teacher.getTeacherCode()));
    }

    private CoursePayload normalizeCourse(CoursePayload course) {
        if (course == null) {
            return null;
        }

        return new CoursePayload(
                CertificateRequestNormalizer.code(course.getCode()),
                CertificateRequestNormalizer.text(course.getSubject()),
                CertificateRequestNormalizer.text(course.getCycle()),
                CertificateRequestNormalizer.code(course.getSection()),
                CertificateRequestNormalizer.code(course.getSchool()),
                CertificateRequestNormalizer.code(course.getPlan()),
                CertificateRequestNormalizer.code(course.getSemester()));
    }

    private IssuerPayload normalizeIssuer(IssuerPayload issuer) {
        if (issuer == null) {
            return null;
        }

        return new IssuerPayload(
                CertificateRequestNormalizer.code(issuer.getSystem()),
                CertificateRequestNormalizer.text(issuer.getExecutedByUserid()),
                CertificateRequestNormalizer.email(issuer.getExecutedByEmail()));
    }
}
