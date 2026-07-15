package pe.edu.unmsm.fisi.gestiondocente.constancia.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CoursePayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.TeacherPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.PdfGenerationException;

@Service
public class PdfBoxPdfGenerationService implements PdfGenerationService {

    private static final float MARGIN = 56F;
    private static final float FOOTER_MARGIN = 44F;
    private static final float FONT_SIZE = 11F;
    private static final float TITLE_FONT_SIZE = 13F;
    private static final float FOOTER_FONT_SIZE = 8F;
    private static final float LEADING = 15F;
    private static final Locale SPANISH = Locale.forLanguageTag("es");

    @Override
    public byte[] generateCourseCertificate(CourseCertificateRequest request, CertificateGenerationMetadata metadata) {
        validateRequiredData(request, metadata);

        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDFont regularFont = loadRegularFont(document);
            PDFont boldFont = loadBoldFont(document);
            PdfWriter writer = new PdfWriter(document, regularFont, boldFont);

            writeCertificate(writer, request, metadata);

            writer.close();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new PdfGenerationException("No se pudo generar el PDF de constancia", exception);
        }
    }

    private void writeCertificate(PdfWriter writer, CourseCertificateRequest request,
            CertificateGenerationMetadata metadata) throws IOException {
        TeacherPayload teacher = request.getTeacher();
        CoursePayload course = request.getCourse();

        writer.writeCenteredTitle("CONSTANCIA DE ELABORACIÓN Y PUBLICACIÓN DE MATERIALES DIDÁCTICOS EN EL AULA VIRTUAL");
        writer.space(18F);
        writer.writeParagraph("A QUIEN CORRESPONDA:", true);
        writer.space(8F);
        writer.writeParagraph("Por medio de la presente se deja constancia que el docente "
                + teacher.getFullName() + ", responsable del curso " + course.getSubject()
                + ", durante el semestre académico " + course.getSemester()
                + ", ha elaborado y publicado oportunamente materiales didácticos en el Aula Virtual institucional, "
                + "destinados a fortalecer el proceso de enseñanza-aprendizaje de los estudiantes.", false);
        writer.space(6F);
        writer.writeParagraph("De acuerdo con los registros del Aula Virtual, se verificó la disponibilidad "
                + "de los siguientes recursos académicos:", false);
        writer.space(8F);
        writer.writeTable(List.of(
                new String[] { "Tipo de material didáctico", "Cumplió" },
                new String[] { "Notas de Curso", "Sí" },
                new String[] { "Guías de práctica por curso", "Sí" },
                new String[] { "Materiales Didácticos Electrónicos", "Sí" }));
        writer.space(12F);
        writer.writeParagraph("Los materiales antes señalados fueron publicados y puestos a disposición "
                + "de los estudiantes mediante el Aula Virtual institucional, constituyendo evidencia del desarrollo "
                + "de recursos educativos de autoría del docente para el cumplimiento de los objetivos de aprendizaje "
                + "del curso.", false);
        writer.space(6F);
        writer.writeParagraph("Se expide la presente constancia a solicitud del interesado, para los fines "
                + "académicos y administrativos que estime convenientes.", false);
        writer.space(14F);
        writer.writeParagraph("Lima, " + formatSpanishDate(metadata.getGeneratedAt().toLocalDate()), false);
        writer.space(18F);
        writer.writeParagraph("Oficina del Aula Virtual", false);
        writer.writeParagraph("Facultad de Ingeniería de Sistemas e Informática", false);
        writer.writeParagraph("Universidad Nacional Mayor de San Marcos", false);
        writer.writeFooter("ID interno: " + metadata.getGenerationId()
                + " | Versión: v" + String.format("%03d", metadata.getVersion())
                + " | Curso: " + course.getCode()
                + " | Sección: " + course.getSection());
    }

    private void validateRequiredData(CourseCertificateRequest request, CertificateGenerationMetadata metadata) {
        if (request == null) {
            throw new PdfGenerationException("La solicitud de constancia es obligatoria");
        }
        if (metadata == null) {
            throw new PdfGenerationException("La metadata de generación es obligatoria");
        }
        if (request.getTeacher() == null) {
            throw new PdfGenerationException("Los datos del docente son obligatorios");
        }
        if (request.getCourse() == null) {
            throw new PdfGenerationException("Los datos del curso son obligatorios");
        }
        requireText(request.getTeacher().getFullName(), "El nombre del docente es obligatorio");
        requireText(request.getCourse().getSubject(), "El nombre del curso es obligatorio");
        requireText(request.getCourse().getSemester(), "El semestre es obligatorio");
        requireText(metadata.getGenerationId(), "El identificador de generación es obligatorio");
        if (metadata.getGeneratedAt() == null) {
            throw new PdfGenerationException("La fecha de generación es obligatoria");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new PdfGenerationException(message);
        }
    }

    private String formatSpanishDate(LocalDate date) {
        Month month = date.getMonth();
        return date.getDayOfMonth() + " de "
                + month.getDisplayName(TextStyle.FULL, SPANISH)
                + " de " + date.getYear();
    }

    private PDFont loadRegularFont(PDDocument document) throws IOException {
        return loadFont(document, List.of(
                "C:/Windows/Fonts/arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf"))
                .orElse(PDType1Font.HELVETICA);
    }

    private PDFont loadBoldFont(PDDocument document) throws IOException {
        return loadFont(document, List.of(
                "C:/Windows/Fonts/arialbd.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
                "/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf"))
                .orElse(PDType1Font.HELVETICA_BOLD);
    }

    private java.util.Optional<PDFont> loadFont(PDDocument document, List<String> candidates) throws IOException {
        for (String candidate : candidates) {
            File fontFile = new File(candidate);
            if (fontFile.isFile()) {
                return java.util.Optional.of(PDType0Font.load(document, fontFile));
            }
        }

        return java.util.Optional.empty();
    }

    private static class PdfWriter {

        private final PDDocument document;
        private final PDFont regularFont;
        private final PDFont boldFont;
        private final float pageWidth;
        private final float pageHeight;
        private PDPageContentStream contentStream;
        private float y;

        PdfWriter(PDDocument document, PDFont regularFont, PDFont boldFont) throws IOException {
            this.document = document;
            this.regularFont = regularFont;
            this.boldFont = boldFont;
            this.pageWidth = PDRectangle.A4.getWidth();
            this.pageHeight = PDRectangle.A4.getHeight();
            newPage();
        }

        void writeCenteredTitle(String text) throws IOException {
            List<String> lines = wrapText(text, boldFont, TITLE_FONT_SIZE, pageWidth - (2 * MARGIN));
            for (String line : lines) {
                ensureSpace(LEADING);
                float textWidth = boldFont.getStringWidth(line) / 1000F * TITLE_FONT_SIZE;
                writeText(line, boldFont, TITLE_FONT_SIZE, (pageWidth - textWidth) / 2F, y);
                y -= LEADING;
            }
        }

        void writeParagraph(String text, boolean bold) throws IOException {
            PDFont font = bold ? boldFont : regularFont;
            List<String> lines = wrapText(text, font, FONT_SIZE, pageWidth - (2 * MARGIN));
            for (String line : lines) {
                ensureSpace(LEADING);
                writeText(line, font, FONT_SIZE, MARGIN, y);
                y -= LEADING;
            }
        }

        void writeTable(List<String[]> rows) throws IOException {
            float tableWidth = pageWidth - (2 * MARGIN);
            float firstColumnWidth = tableWidth * 0.76F;
            float rowHeight = 20F;

            for (int i = 0; i < rows.size(); i++) {
                ensureSpace(rowHeight + 4F);
                String[] row = rows.get(i);
                PDFont font = i == 0 ? boldFont : regularFont;
                writeText(row[0], font, FONT_SIZE, MARGIN, y);
                writeText(row[1], font, FONT_SIZE, MARGIN + firstColumnWidth, y);
                y -= rowHeight;
            }
        }

        void writeFooter(String text) throws IOException {
            float oldY = y;
            y = FOOTER_MARGIN;
            List<String> lines = wrapText(text, regularFont, FOOTER_FONT_SIZE, pageWidth - (2 * MARGIN));
            for (String line : lines) {
                writeText(line, regularFont, FOOTER_FONT_SIZE, MARGIN, y);
                y -= 11F;
            }
            y = oldY;
        }

        void space(float amount) throws IOException {
            ensureSpace(amount);
            y -= amount;
        }

        void close() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
        }

        private void newPage() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            y = pageHeight - MARGIN;
        }

        private void ensureSpace(float requiredSpace) throws IOException {
            if (y - requiredSpace < FOOTER_MARGIN + 24F) {
                newPage();
            }
        }

        private void writeText(String text, PDFont font, float fontSize, float x, float lineY) throws IOException {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, lineY);
            contentStream.showText(text);
            contentStream.endText();
        }

        private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
            List<String> lines = new ArrayList<>();
            StringBuilder currentLine = new StringBuilder();

            for (String word : text.split(" ")) {
                String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
                float width = font.getStringWidth(candidate) / 1000F * fontSize;

                if (width <= maxWidth) {
                    currentLine = new StringBuilder(candidate);
                } else {
                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine.toString());
                    }
                    currentLine = new StringBuilder(word);
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }

            return lines;
        }
    }
}
