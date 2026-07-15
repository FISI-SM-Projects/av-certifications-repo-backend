package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CoursePayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.IssuerPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.TeacherPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingRequiredFieldsException;

class CourseCertificateRequestValidatorTest {

    private final CourseCertificateRequestValidator validator = new CourseCertificateRequestValidator();

    @Test
    void solicitudCompletaDebeSerValida() {
        assertThatCode(() -> validator.validate(validRequest())).doesNotThrowAnyException();
    }

    @Test
    void teacherAusenteDebeSerInvalido() {
        CourseCertificateRequest request = validRequest();
        request.setTeacher(null);

        assertMissingFields(request, "teacher");
    }

    @Test
    void courseAusenteDebeSerInvalido() {
        CourseCertificateRequest request = validRequest();
        request.setCourse(null);

        assertMissingFields(request, "course");
    }

    @Test
    void issuerAusenteDebeSerInvalido() {
        CourseCertificateRequest request = validRequest();
        request.setIssuer(null);

        assertMissingFields(request, "issuer");
    }

    @ParameterizedTest
    @MethodSource("invalidFieldMutations")
    void campoIndividualNuloDebeSerReportado(InvalidFieldMutation mutation) {
        CourseCertificateRequest request = validRequest();

        mutation.mutate(request, null);

        assertMissingFields(request, mutation.field());
    }

    @ParameterizedTest
    @MethodSource("invalidFieldMutations")
    void campoIndividualVacioDebeSerReportado(InvalidFieldMutation mutation) {
        CourseCertificateRequest request = validRequest();

        mutation.mutate(request, "");

        assertMissingFields(request, mutation.field());
    }

    @ParameterizedTest
    @MethodSource("invalidFieldMutations")
    void campoIndividualConSoloEspaciosDebeSerReportado(InvalidFieldMutation mutation) {
        CourseCertificateRequest request = validRequest();

        mutation.mutate(request, "   ");

        assertMissingFields(request, mutation.field());
    }

    @Test
    void debeReportarTodosLosCamposInvalidosEnUnaSolaValidacion() {
        CourseCertificateRequest request = validRequest();
        request.getTeacher().setEmail(" ");
        request.getCourse().setSection(null);
        request.getIssuer().setExecutedByEmail("");

        assertMissingFields(request, "teacher.email", "course.section", "issuer.executed_by_email");
    }

    private void assertMissingFields(CourseCertificateRequest request, String... fields) {
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOfSatisfying(MissingRequiredFieldsException.class, exception ->
                        org.assertj.core.api.Assertions.assertThat(exception.getMissingFields())
                                .containsExactly(fields))
                .hasMessage(MissingRequiredFieldsException.DEFAULT_MESSAGE);
    }

    private static CourseCertificateRequest validRequest() {
        return new CourseCertificateRequest(
                new TeacherPayload("Nombre completo docente", "correodocente@unmsm.edu.pe", "22200275"),
                new CoursePayload("32BGNYGF", "Nombre del curso", "7", "1", "SW", "2023", "26.1"),
                new IssuerPayload("moodle", "12345", "usuario@unmsm.edu.pe"));
    }

    private static java.util.stream.Stream<InvalidFieldMutation> invalidFieldMutations() {
        return java.util.stream.Stream.of(
                new InvalidFieldMutation("teacher.full_name", (request, value) ->
                        request.getTeacher().setFullName(value)),
                new InvalidFieldMutation("teacher.email", (request, value) ->
                        request.getTeacher().setEmail(value)),
                new InvalidFieldMutation("teacher.teacher_code", (request, value) ->
                        request.getTeacher().setTeacherCode(value)),
                new InvalidFieldMutation("course.code", (request, value) ->
                        request.getCourse().setCode(value)),
                new InvalidFieldMutation("course.subject", (request, value) ->
                        request.getCourse().setSubject(value)),
                new InvalidFieldMutation("course.cycle", (request, value) ->
                        request.getCourse().setCycle(value)),
                new InvalidFieldMutation("course.section", (request, value) ->
                        request.getCourse().setSection(value)),
                new InvalidFieldMutation("course.school", (request, value) ->
                        request.getCourse().setSchool(value)),
                new InvalidFieldMutation("course.plan", (request, value) ->
                        request.getCourse().setPlan(value)),
                new InvalidFieldMutation("course.semester", (request, value) ->
                        request.getCourse().setSemester(value)),
                new InvalidFieldMutation("issuer.system", (request, value) ->
                        request.getIssuer().setSystem(value)),
                new InvalidFieldMutation("issuer.executed_by_userid", (request, value) ->
                        request.getIssuer().setExecutedByUserid(value)),
                new InvalidFieldMutation("issuer.executed_by_email", (request, value) ->
                        request.getIssuer().setExecutedByEmail(value)));
    }

    private record InvalidFieldMutation(String field, BiConsumerWithValue mutator) {

        void mutate(CourseCertificateRequest request, String value) {
            mutator.accept(request, value);
        }
    }

    @FunctionalInterface
    private interface BiConsumerWithValue extends Consumer<CourseCertificateRequest> {

        void accept(CourseCertificateRequest request, String value);

        @Override
        default void accept(CourseCertificateRequest request) {
            accept(request, null);
        }
    }
}
