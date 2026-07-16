package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.ExpectedCourseRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.SemesterCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.DuplicateExpectedCoursesException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidRequestFieldsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingRequiredFieldsException;

class SemesterCertificateRequestValidatorTest {

    private final SemesterCertificateRequestValidator validator = new SemesterCertificateRequestValidator();

    @Test
    void solicitudCompletaDebeSerValida() {
        assertThatCode(() -> validator.validate(validRequest())).doesNotThrowAnyException();
    }

    @Test
    void requestNuloDebeReportarCamposObligatorios() {
        assertMissingFields(null, "teacher_code", "semester", "expected_courses");
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

    @Test
    void cursosDuplicadosDebenSerRechazadosDespuesDeNormalizar() {
        SemesterCertificateRequest request = new SemesterCertificateRequest(
                "22200275",
                "26.1",
                List.of(
                        new ExpectedCourseRequest("curso-a", "1"),
                        new ExpectedCourseRequest(" CURSO-A ", "1 ")));

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOfSatisfying(DuplicateExpectedCoursesException.class, exception ->
                        org.assertj.core.api.Assertions.assertThat(exception.getDuplicateCourses())
                                .extracting(ExpectedCourseRequest::getCode, ExpectedCourseRequest::getSection)
                                .containsExactly(org.assertj.core.api.Assertions.tuple("CURSO-A", "1")));
    }

    @Test
    void listaSobreMaximoDebeSerInvalida() {
        List<ExpectedCourseRequest> expectedCourses = new ArrayList<>();
        for (int index = 0; index <= CertificateRequestValidationRules.EXPECTED_COURSES_MAX; index++) {
            expectedCourses.add(new ExpectedCourseRequest("C" + index, "1"));
        }

        SemesterCertificateRequest request = new SemesterCertificateRequest("22200275", "26.1", expectedCourses);

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOfSatisfying(InvalidRequestFieldsException.class, exception ->
                        org.assertj.core.api.Assertions.assertThat(exception.getInvalidFields())
                                .extracting(InvalidRequestFieldsException.InvalidField::field)
                                .contains("expected_courses"));
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
