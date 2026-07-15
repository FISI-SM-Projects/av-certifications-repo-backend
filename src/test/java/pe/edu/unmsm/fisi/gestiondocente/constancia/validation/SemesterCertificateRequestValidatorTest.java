package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.ExpectedCourseRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.SemesterCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingRequiredFieldsException;

class SemesterCertificateRequestValidatorTest {

    private final SemesterCertificateRequestValidator validator = new SemesterCertificateRequestValidator();

    @Test
    void solicitudCompletaDebeSerValida() {
        assertThatCode(() -> validator.validate(validRequest())).doesNotThrowAnyException();
    }

    @Test
    void teacherCodeAusenteDebeSerInvalido() {
        SemesterCertificateRequest request = validRequest();
        request.setTeacherCode(null);

        assertMissingFields(request, "teacher_code");
    }

    @Test
    void semesterAusenteDebeSerInvalido() {
        SemesterCertificateRequest request = validRequest();
        request.setSemester(null);

        assertMissingFields(request, "semester");
    }

    @Test
    void listaDeCursosNullDebeSerInvalida() {
        SemesterCertificateRequest request = validRequest();
        request.setExpectedCourses(null);

        assertMissingFields(request, "expected_courses");
    }

    @Test
    void listaDeCursosVaciaDebeSerInvalida() {
        SemesterCertificateRequest request = validRequest();
        request.setExpectedCourses(List.of());

        assertMissingFields(request, "expected_courses");
    }

    @Test
    void elementoNuloDebeSerInvalido() {
        SemesterCertificateRequest request = validRequest();
        List<ExpectedCourseRequest> expectedCourses = new ArrayList<>();
        expectedCourses.add(null);
        request.setExpectedCourses(expectedCourses);

        assertMissingFields(request, "expected_courses[0]");
    }

    @Test
    void cursoSinCodeDebeSerInvalido() {
        SemesterCertificateRequest request = validRequest();
        request.getExpectedCourses().get(0).setCode(null);

        assertMissingFields(request, "expected_courses[0].code");
    }

    @Test
    void cursoSinSectionDebeSerInvalido() {
        SemesterCertificateRequest request = validRequest();
        request.getExpectedCourses().get(0).setSection(null);

        assertMissingFields(request, "expected_courses[0].section");
    }

    @Test
    void multiplesErroresDebenReportarseALaVez() {
        SemesterCertificateRequest request = new SemesterCertificateRequest(
                " ",
                null,
                List.of(new ExpectedCourseRequest("", " "), new ExpectedCourseRequest(null, null)));

        assertMissingFields(request,
                "teacher_code",
                "semester",
                "expected_courses[0].code",
                "expected_courses[0].section",
                "expected_courses[1].code",
                "expected_courses[1].section");
    }

    private void assertMissingFields(SemesterCertificateRequest request, String... fields) {
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOfSatisfying(MissingRequiredFieldsException.class, exception ->
                        org.assertj.core.api.Assertions.assertThat(exception.getMissingFields())
                                .containsExactly(fields))
                .hasMessage(MissingRequiredFieldsException.DEFAULT_MESSAGE);
    }

    private static SemesterCertificateRequest validRequest() {
        return new SemesterCertificateRequest(
                "22200275",
                "26.1",
                new ArrayList<>(List.of(
                        new ExpectedCourseRequest("32BGNYGF", "1"),
                        new ExpectedCourseRequest("32SW001", "2"))));
    }
}
