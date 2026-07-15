package pe.edu.unmsm.fisi.gestiondocente.constancia.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CoursePayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.IssuerPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.TeacherPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.PdfGenerationException;

class PdfBoxPdfGenerationServiceTest {

    private final PdfGenerationService pdfGenerationService = new PdfBoxPdfGenerationService();

    @TempDir
    private Path tempDir;

    @Test
    void debeGenerarPdfValidoEnMemoria() {
        byte[] pdf = pdfGenerationService.generateCourseCertificate(validRequest(), validMetadata());

        assertThat(pdf).isNotNull();
        assertThat(pdf).isNotEmpty();
        assertThat(pdf.length).isGreaterThan(1_000);
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void debeContenerTextoInstitucionalYDatosDinamicos() throws Exception {
        byte[] pdf = pdfGenerationService.generateCourseCertificate(validRequest(), validMetadata());

        String text = extractText(pdf);
        String normalizedText = text.replaceAll("\\s+", " ");

        assertThat(normalizedText).contains("CONSTANCIA DE ELABORACIÓN Y PUBLICACIÓN DE MATERIALES DIDÁCTICOS EN EL AULA VIRTUAL");
        assertThat(text).contains("José Muñoz Peña");
        assertThat(text).contains("Ingeniería y Gestión de Proyectos");
        assertThat(text).contains("2026-I");
        assertThat(text).contains("Oficina del Aula Virtual");
        assertThat(text).contains("Universidad Nacional Mayor de San Marcos");
        assertThat(text).contains("22200275-32BGNYGF-1-2026-I-v001");
    }

    @Test
    void debeConservarCaracteresEspanolesEnTextoExtraido() throws Exception {
        byte[] pdf = pdfGenerationService.generateCourseCertificate(validRequest(), validMetadata());

        String text = extractText(pdf);

        assertThat(text).contains("José");
        assertThat(text).contains("Muñoz");
        assertThat(text).contains("Ingeniería");
        assertThat(text).contains("Gestión");
        assertThat(text).contains("didácticos");
        assertThat(text).contains("académico");
    }

    @Test
    void debeUsarFechaDeMetadataEnEspanol() throws Exception {
        byte[] pdf = pdfGenerationService.generateCourseCertificate(validRequest(), validMetadata());

        String text = extractText(pdf);

        assertThat(text).contains("14 de julio de 2026");
    }

    @Test
    void debeIncluirTablaDeMateriales() throws Exception {
        byte[] pdf = pdfGenerationService.generateCourseCertificate(validRequest(), validMetadata());

        String text = extractText(pdf);

        assertThat(text).contains("Notas de Curso");
        assertThat(text).contains("Guías de práctica por curso");
        assertThat(text).contains("Materiales Didácticos Electrónicos");
        assertThat(text).contains("Sí");
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void debeFallarConDatosImprescindiblesInvalidos(InvalidInput invalidInput) {
        CourseCertificateRequest request = validRequest();
        CertificateGenerationMetadata metadata = validMetadata();

        invalidInput.apply(request, metadata);
        CourseCertificateRequest finalRequest = invalidInput.nullRequest() ? null : request;
        CertificateGenerationMetadata finalMetadata = invalidInput.nullMetadata() ? null : metadata;

        assertThatThrownBy(() -> pdfGenerationService.generateCourseCertificate(finalRequest, finalMetadata))
                .isInstanceOf(PdfGenerationException.class)
                .hasMessageContaining(invalidInput.expectedMessage());
    }

    @Test
    void noDebeCrearArchivosDuranteLaGeneracion() {
        assertThatCode(() -> pdfGenerationService.generateCourseCertificate(validRequest(), validMetadata()))
                .doesNotThrowAnyException();

        assertThat(listTempFiles()).isEmpty();
    }

    private List<Path> listTempFiles() {
        try (java.util.stream.Stream<Path> paths = Files.walk(tempDir)) {
            return paths.filter(path -> !path.equals(tempDir)).toList();
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String extractText(byte[] pdf) throws IOException {
        try (PDDocument document = PDDocument.load(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static java.util.stream.Stream<InvalidInput> invalidInputs() {
        return java.util.stream.Stream.of(
                new InvalidInput("solicitud", (request, metadata) -> {
                }, "La solicitud de constancia es obligatoria", true, false),
                new InvalidInput("metadata", (request, metadata) -> {
                }, "La metadata de generación es obligatoria", false, true),
                new InvalidInput("teacher", (request, metadata) -> request.setTeacher(null),
                        "Los datos del docente son obligatorios"),
                new InvalidInput("course", (request, metadata) -> request.setCourse(null),
                        "Los datos del curso son obligatorios"),
                new InvalidInput("teacherName", (request, metadata) -> request.getTeacher().setFullName(" "),
                        "El nombre del docente es obligatorio"),
                new InvalidInput("courseSubject", (request, metadata) -> request.getCourse().setSubject(""),
                        "El nombre del curso es obligatorio"),
                new InvalidInput("semester", (request, metadata) -> request.getCourse().setSemester(null),
                        "El semestre es obligatorio"),
                new InvalidInput("generationId", (request, metadata) -> metadata.setGenerationId(" "),
                        "El identificador de generación es obligatorio"),
                new InvalidInput("generatedAt", (request, metadata) -> metadata.setGeneratedAt(null),
                        "La fecha de generación es obligatoria"));
    }

    private CourseCertificateRequest validRequest() {
        return new CourseCertificateRequest(
                new TeacherPayload("José Muñoz Peña", "jmunoz@unmsm.edu.pe", "22200275"),
                new CoursePayload("32BGNYGF", "Ingeniería y Gestión de Proyectos", "7", "1", "SW", "2023",
                        "2026-I"),
                new IssuerPayload("moodle", "12345", "usuario@unmsm.edu.pe"));
    }

    private CertificateGenerationMetadata validMetadata() {
        return new CertificateGenerationMetadata(
                "22200275-32BGNYGF-1-2026-I-v001",
                "22200275-32BGNYGF-1-2026-I",
                1,
                TipoConstancia.CURSO,
                EstadoConstancia.GENERADO,
                "22200275",
                "32BGNYGF",
                "1",
                "2026-I",
                LocalDateTime.of(2026, 7, 14, 10, 30),
                "request.json",
                "certificate.pdf");
    }

    private record InvalidInput(String name, InvalidInputMutation mutation, String expectedMessage,
            boolean nullRequest, boolean nullMetadata) {

        InvalidInput(String name, InvalidInputMutation mutation, String expectedMessage) {
            this(name, mutation, expectedMessage, false, false);
        }

        void apply(CourseCertificateRequest request, CertificateGenerationMetadata metadata) {
            mutation.accept(request, metadata);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @FunctionalInterface
    private interface InvalidInputMutation {

        void accept(CourseCertificateRequest request, CertificateGenerationMetadata metadata);
    }
}
