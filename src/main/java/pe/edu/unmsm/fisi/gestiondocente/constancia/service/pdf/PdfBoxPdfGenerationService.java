package pe.edu.unmsm.fisi.gestiondocente.constancia.service.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.awt.Color;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.SemesterCertificateSource;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.SemesterCertificateSourceSummary;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.TeacherPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.pdf.PdfGenerationService;
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
    private static final float HEADER_BOTTOM_Y = PDRectangle.A4.getHeight() - 118F;
    private static final Color UNMSM_RED = new Color(128, 0, 32);
    private static final Color TABLE_HEADER_GRAY = new Color(238, 238, 238);
    private static final Color TABLE_BORDER_GRAY = new Color(80, 80, 80);
    private static final Locale SPANISH = Locale.forLanguageTag("es");
    private static final ZoneId LIMA_ZONE = ZoneId.of("America/Lima");

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
        } catch (IOException | IllegalArgumentException exception) {
            throw new PdfGenerationException("No se pudo generar el PDF de constancia", exception);
        }
    }

    @Override
    public byte[] generateSemesterCertificate(SemesterCertificateSourceSummary sourceSummary,
            CertificateGenerationMetadata metadata) {
        validateSemesterData(sourceSummary, metadata);

        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDFont regularFont = loadRegularFont(document);
            PDFont boldFont = loadBoldFont(document);
            PdfWriter writer = new PdfWriter(document, regularFont, boldFont);

            writeSemesterCertificate(writer, sourceSummary, metadata);

            writer.close();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException | IllegalArgumentException exception) {
            throw new PdfGenerationException("No se pudo generar el PDF de constancia semestral", exception);
        }
    }

    @Override
    public byte[] addVisibleInstitutionalSignature(byte[] originalPdf, String directorName, String directorCode,
            String department, Instant signedAt) {
        if (originalPdf == null || originalPdf.length == 0) {
            throw new PdfGenerationException("El PDF original es obligatorio para firmar la constancia");
        }

        try (PDDocument document = PDDocument.load(originalPdf);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDFont regularFont = loadRegularFont(document);
            PDFont boldFont = loadBoldFont(document);
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                writeSignaturePageHeader(content, regularFont, boldFont);
                float y = HEADER_BOTTOM_Y;
                writeVisibleSignatureLine(content, "FIRMA VISIBLE INSTITUCIONAL", boldFont, 15F, MARGIN, y);
                y -= 32F;
                writeVisibleSignatureLine(content, "Firmado digitalmente por:", regularFont, 11F, MARGIN, y);
                y -= 20F;
                writeVisibleSignatureLine(content, signatureText(directorName, "Director"), boldFont, 12F, MARGIN, y);
                y -= 18F;
                writeVisibleSignatureLine(content, "Codigo/Cuenta: " + signatureText(directorCode, "No registrado"), regularFont, 10F, MARGIN, y);
                y -= 18F;
                writeVisibleSignatureLine(content, "Cargo: Director de Departamento Academico", regularFont, 10F, MARGIN, y);
                y -= 18F;
                writeVisibleSignatureLine(content, "Departamento: " + signatureText(department, "No registrado"), regularFont, 10F, MARGIN, y);
                y -= 18F;
                writeVisibleSignatureLine(content, "Fecha de firma: " + DateTimeFormatter.ISO_OFFSET_DATE_TIME
                        .format((signedAt == null ? Instant.now() : signedAt).atZone(LIMA_ZONE)), regularFont, 10F, MARGIN, y);
                y -= 34F;
                writeVisibleSignatureLine(content, "Firma visible institucional no criptografica.", boldFont, 11F, MARGIN, y);
                y -= 18F;
                writeVisibleSignatureLine(content, "Este bloque deja constancia visual de la aprobacion del director en el Sistema de Constancias FISI.",
                        regularFont, 9F, MARGIN, y);
                writeSignaturePageFooter(content, regularFont);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException | IllegalArgumentException exception) {
            throw new PdfGenerationException("No se pudo agregar la firma visible institucional", exception);
        }
    }

    private void writeCertificate(PdfWriter writer, CourseCertificateRequest request,
            CertificateGenerationMetadata metadata) throws IOException {
        TeacherPayload teacher = request.getTeacher();
        CoursePayload course = request.getCourse();

        writer.writeCenteredTitle("CONSTANCIA DE CARGA ACADÉMICA");
        writer.writeCenteredSubtitle("Elaboración y publicación de materiales didácticos en el Aula Virtual");
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
        writer.writeParagraph("Lima, " + formatSpanishDate(generatedDateInLima(metadata)), false);
        writer.space(18F);
        writer.writeParagraph("Oficina del Aula Virtual", false);
        writer.writeParagraph("Facultad de Ingeniería de Sistemas e Informática", false);
        writer.writeParagraph("Universidad Nacional Mayor de San Marcos", false);
        writer.writeFooter("Documento emitido por el Sistema de Constancias FISI. ID interno: " + metadata.getGenerationId()
                + " | Versión: v" + String.format("%03d", metadata.getVersion())
                + " | Curso: " + course.getCode()
                + " | Sección: " + course.getSection());
    }

    private void writeSemesterCertificate(PdfWriter writer, SemesterCertificateSourceSummary sourceSummary,
            CertificateGenerationMetadata metadata) throws IOException {
        writer.writeCenteredTitle(
                "CONSTANCIA SEMESTRAL DE ELABORACIÓN Y PUBLICACIÓN DE MATERIALES DIDÁCTICOS EN EL AULA VIRTUAL");
        writer.writeCenteredSubtitle("Consolidado de constancias por curso registradas en el periodo académico");
        writer.space(16F);
        writer.writeParagraph("A QUIEN CORRESPONDA:", true);
        writer.space(8F);
        writer.writeParagraph("Por medio de la presente se deja constancia que el docente "
                + sourceSummary.getTeacherFullName() + ", identificado con código docente "
                + sourceSummary.getTeacherCode() + ", cuenta con constancias por curso registradas para el periodo "
                + "académico " + sourceSummary.getSemester() + ".", false);
        writer.space(6F);
        writer.writeParagraph("La presente constancia semestral consolida "
                + sourceSummary.getSourceGenerations().size()
                + " cursos con materiales didácticos elaborados y publicados en el Aula Virtual institucional.",
                false);
        writer.space(10F);
        writer.writeSemesterTable(sourceSummary.getSourceGenerations());
        writer.space(14F);
        writer.writeParagraph("Lima, " + formatSpanishDate(generatedDateInLima(metadata)), false);
        writer.space(18F);
        writer.writeParagraph("Oficina del Aula Virtual", false);
        writer.writeParagraph("Facultad de Ingeniería de Sistemas e Informática", false);
        writer.writeParagraph("Universidad Nacional Mayor de San Marcos", false);
        writer.writeFooter("Documento emitido por el Sistema de Constancias FISI. ID interno: " + metadata.getGenerationId()
                + " | Versión: v" + String.format("%03d", metadata.getVersion())
                + " | Periodo: " + sourceSummary.getSemester());
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

    private void validateSemesterData(SemesterCertificateSourceSummary sourceSummary,
            CertificateGenerationMetadata metadata) {
        if (sourceSummary == null) {
            throw new PdfGenerationException("El resumen de fuentes es obligatorio");
        }
        if (metadata == null) {
            throw new PdfGenerationException("La metadata de generación es obligatoria");
        }
        requireText(sourceSummary.getTeacherCode(), "El código docente es obligatorio");
        requireText(sourceSummary.getTeacherFullName(), "El nombre del docente es obligatorio");
        requireText(sourceSummary.getSemester(), "El semestre es obligatorio");
        if (sourceSummary.getSourceGenerations() == null || sourceSummary.getSourceGenerations().isEmpty()) {
            throw new PdfGenerationException("Las fuentes de constancia semestral son obligatorias");
        }
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

    private LocalDate generatedDateInLima(CertificateGenerationMetadata metadata) {
        return metadata.getGeneratedAt().atZone(LIMA_ZONE).toLocalDate();
    }

    private void writeVisibleSignatureLine(PDPageContentStream content, String text, PDFont font, float fontSize,
            float x, float y) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(stripAccents(text));
        content.endText();
    }

    private void writeSignaturePageHeader(PDPageContentStream content, PDFont regularFont, PDFont boldFont)
            throws IOException {
        float pageWidth = PDRectangle.A4.getWidth();
        float pageHeight = PDRectangle.A4.getHeight();
        float centerY = pageHeight - 36F;
        content.setStrokingColor(UNMSM_RED);
        content.setLineWidth(1.1F);
        content.moveTo(MARGIN, pageHeight - 104F);
        content.lineTo(pageWidth - MARGIN, pageHeight - 104F);
        content.stroke();
        content.setNonStrokingColor(UNMSM_RED);
        content.addRect(MARGIN, pageHeight - 48F, 36F, 28F);
        content.fill();
        content.setNonStrokingColor(Color.WHITE);
        writeVisibleSignatureLine(content, "UNMSM", boldFont, 7F, MARGIN + 4F, pageHeight - 36F);
        content.setNonStrokingColor(Color.BLACK);
        writeCenteredSignatureLine(content, "UNIVERSIDAD NACIONAL MAYOR DE SAN MARCOS", boldFont, 12F, centerY);
        writeCenteredSignatureLine(content, "Universidad del Peru. Decana de America", regularFont, 9F, centerY - 14F);
        writeCenteredSignatureLine(content, "FACULTAD DE INGENIERIA DE SISTEMAS E INFORMATICA", boldFont, 10F, centerY - 29F);
        writeCenteredSignatureLine(content, "Sistema de Constancias Docentes - Aula Virtual FISI", regularFont, 8.5F, centerY - 43F);
    }

    private void writeSignaturePageFooter(PDPageContentStream content, PDFont regularFont) throws IOException {
        float pageWidth = PDRectangle.A4.getWidth();
        content.setStrokingColor(TABLE_BORDER_GRAY);
        content.setLineWidth(0.5F);
        content.moveTo(MARGIN, FOOTER_MARGIN + 19F);
        content.lineTo(pageWidth - MARGIN, FOOTER_MARGIN + 19F);
        content.stroke();
        content.setNonStrokingColor(Color.BLACK);
        writeVisibleSignatureLine(content,
                "Documento firmado visualmente por el Sistema de Constancias FISI. Firma institucional no criptografica.",
                regularFont, FOOTER_FONT_SIZE, MARGIN, FOOTER_MARGIN + 7F);
    }

    private void writeCenteredSignatureLine(PDPageContentStream content, String text, PDFont font, float fontSize,
            float y) throws IOException {
        float textWidth = font.getStringWidth(stripAccents(text)) / 1000F * fontSize;
        writeVisibleSignatureLine(content, text, font, fontSize, (PDRectangle.A4.getWidth() - textWidth) / 2F, y);
    }

    private String signatureText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String stripAccents(String value) {
        return value == null ? "" : value
                .replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u')
                .replace('Á', 'A').replace('É', 'E').replace('Í', 'I').replace('Ó', 'O').replace('Ú', 'U')
                .replace('ñ', 'n').replace('Ñ', 'N');
    }

    private PDFont loadRegularFont(PDDocument document) throws IOException {
        java.util.Optional<PDFont> classpathFont = loadClasspathFont(document, "/fonts/NotoSans-Regular.ttf");
        if (classpathFont.isPresent()) {
            return classpathFont.get();
        }

        return loadFont(document, List.of(
                "C:/Windows/Fonts/arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf"))
                .orElse(PDType1Font.HELVETICA);
    }

    private PDFont loadBoldFont(PDDocument document) throws IOException {
        java.util.Optional<PDFont> classpathFont = loadClasspathFont(document, "/fonts/NotoSans-Bold.ttf");
        if (classpathFont.isPresent()) {
            return classpathFont.get();
        }

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

    private java.util.Optional<PDFont> loadClasspathFont(PDDocument document, String resourcePath) throws IOException {
        try (InputStream inputStream = PdfBoxPdfGenerationService.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return java.util.Optional.empty();
            }

            return java.util.Optional.of(PDType0Font.load(document, inputStream));
        }
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
                float textWidth = stringWidth(line, boldFont, TITLE_FONT_SIZE);
                writeText(line, boldFont, TITLE_FONT_SIZE, (pageWidth - textWidth) / 2F, y);
                y -= LEADING;
            }
        }

        void writeCenteredSubtitle(String text) throws IOException {
            List<String> lines = wrapText(text, regularFont, 10F, pageWidth - (2 * MARGIN));
            for (String line : lines) {
                ensureSpace(12F);
                float textWidth = stringWidth(line, regularFont, 10F);
                writeText(line, regularFont, 10F, (pageWidth - textWidth) / 2F, y);
                y -= 12F;
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
            float[] widths = new float[] { firstColumnWidth, tableWidth - firstColumnWidth };

            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                drawTableRow(row, widths, i == 0, FONT_SIZE, 26F);
            }
        }

        void writeSemesterTable(List<SemesterCertificateSource> sources) throws IOException {
            float tableWidth = pageWidth - (2 * MARGIN);
            float[] widths = new float[] {
                    tableWidth * 0.14F,
                    tableWidth * 0.33F,
                    tableWidth * 0.10F,
                    tableWidth * 0.10F,
                    tableWidth * 0.10F,
                    tableWidth * 0.15F
            };
            writeSemesterTableRow(
                    new String[] { "Código", "Nombre del curso", "Sección", "Escuela", "Plan", "Estado" },
                    widths,
                    true);

            for (SemesterCertificateSource source : sources) {
                String[] values = new String[] {
                        source.getCourseCode(),
                        source.getCourseSubject(),
                        source.getSection(),
                        source.getSchool(),
                        source.getPlan(),
                        source.getStatus().name()
                };
                if (needsNewPageForSemesterRow(values, widths, false)) {
                    newPage();
                    writeSemesterTableRow(
                            new String[] { "Codigo", "Nombre del curso", "Seccion", "Escuela", "Plan", "Estado" },
                            widths,
                            true);
                }
                writeSemesterTableRow(values, widths, false);
            }
        }

        private void writeSemesterTableRow(String[] values, float[] widths, boolean header) throws IOException {
            float fontSize = header ? 8.5F : 8F;
            drawTableRow(values, widths, header, fontSize, 28F);
        }

        private boolean needsNewPageForSemesterRow(String[] values, float[] widths, boolean header) throws IOException {
            return y - semesterRowHeight(values, widths, header) < FOOTER_MARGIN + 24F;
        }

        private float semesterRowHeight(String[] values, float[] widths, boolean header) throws IOException {
            PDFont font = header ? boldFont : regularFont;
            float fontSize = header ? 8.5F : 8F;
            int maxLines = 1;
            for (int i = 0; i < values.length; i++) {
                maxLines = Math.max(maxLines, wrapText(values[i], font, fontSize, widths[i] - 4F).size());
            }
            return Math.max(28F, (maxLines * 10F) + 8F);
        }

        void writeFooter(String text) throws IOException {
            float oldY = y;
            y = FOOTER_MARGIN;
            writeInstitutionalFooter();
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
            writeInstitutionalHeader();
            y = HEADER_BOTTOM_Y;
        }

        private void writeInstitutionalHeader() throws IOException {
            float centerY = pageHeight - 36F;
            contentStream.setStrokingColor(UNMSM_RED);
            contentStream.setLineWidth(1.1F);
            contentStream.moveTo(MARGIN, pageHeight - 104F);
            contentStream.lineTo(pageWidth - MARGIN, pageHeight - 104F);
            contentStream.stroke();
            contentStream.setNonStrokingColor(UNMSM_RED);
            contentStream.addRect(MARGIN, pageHeight - 48F, 36F, 28F);
            contentStream.fill();
            contentStream.setNonStrokingColor(Color.WHITE);
            writeText("UNMSM", boldFont, 7F, MARGIN + 4F, pageHeight - 36F);
            contentStream.setNonStrokingColor(Color.BLACK);
            writeCenteredHeaderLine("UNIVERSIDAD NACIONAL MAYOR DE SAN MARCOS", boldFont, 12F, centerY);
            writeCenteredHeaderLine("Universidad del Peru. Decana de America", regularFont, 9F, centerY - 14F);
            writeCenteredHeaderLine("FACULTAD DE INGENIERIA DE SISTEMAS E INFORMATICA", boldFont, 10F, centerY - 29F);
            writeCenteredHeaderLine("Sistema de Constancias Docentes - Aula Virtual FISI", regularFont, 8.5F, centerY - 43F);
        }

        private void writeInstitutionalFooter() throws IOException {
            contentStream.setStrokingColor(TABLE_BORDER_GRAY);
            contentStream.setLineWidth(0.5F);
            contentStream.moveTo(MARGIN, FOOTER_MARGIN + 19F);
            contentStream.lineTo(pageWidth - MARGIN, FOOTER_MARGIN + 19F);
            contentStream.stroke();
            contentStream.setNonStrokingColor(Color.BLACK);
            writeText("Verifique la autenticidad del documento con el identificador interno y el registro institucional correspondiente.",
                    regularFont, FOOTER_FONT_SIZE, MARGIN, FOOTER_MARGIN + 7F);
        }

        private void writeCenteredHeaderLine(String text, PDFont font, float fontSize, float lineY) throws IOException {
            float textWidth = stringWidth(text, font, fontSize);
            writeText(text, font, fontSize, (pageWidth - textWidth) / 2F, lineY);
        }

        private void drawTableRow(String[] values, float[] widths, boolean header, float fontSize, float minHeight)
                throws IOException {
            PDFont font = header ? boldFont : regularFont;
            List<List<String>> wrappedCells = new ArrayList<>();
            int maxLines = 1;
            for (int i = 0; i < values.length; i++) {
                List<String> lines = wrapText(values[i], font, fontSize, widths[i] - 10F);
                wrappedCells.add(lines);
                maxLines = Math.max(maxLines, lines.size());
            }

            float lineHeight = fontSize + 3F;
            float rowHeight = Math.max(minHeight, (maxLines * lineHeight) + 10F);
            ensureSpace(rowHeight + 4F);
            float topY = y;
            float bottomY = topY - rowHeight;

            if (header) {
                contentStream.setNonStrokingColor(TABLE_HEADER_GRAY);
                contentStream.addRect(MARGIN, bottomY, totalWidth(widths), rowHeight);
                contentStream.fill();
                contentStream.setNonStrokingColor(Color.BLACK);
            }

            contentStream.setStrokingColor(TABLE_BORDER_GRAY);
            contentStream.setLineWidth(0.65F);
            float x = MARGIN;
            contentStream.addRect(MARGIN, bottomY, totalWidth(widths), rowHeight);
            contentStream.stroke();
            for (int i = 0; i < widths.length - 1; i++) {
                x += widths[i];
                contentStream.moveTo(x, topY);
                contentStream.lineTo(x, bottomY);
                contentStream.stroke();
            }

            x = MARGIN;
            for (int i = 0; i < values.length; i++) {
                List<String> lines = wrappedCells.get(i);
                float lineY = topY - 13F;
                for (String line : lines) {
                    writeText(line, font, fontSize, x + 5F, lineY);
                    lineY -= lineHeight;
                }
                x += widths[i];
            }

            y -= rowHeight;
        }

        private float totalWidth(float[] widths) {
            float total = 0F;
            for (float width : widths) {
                total += width;
            }
            return total;
        }

        private void ensureSpace(float requiredSpace) throws IOException {
            if (y - requiredSpace < FOOTER_MARGIN + 24F) {
                newPage();
            }
        }

        private void writeText(String text, PDFont font, float fontSize, float x, float lineY) throws IOException {
            String safeText = sanitizeForFont(text, font);
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, lineY);
            contentStream.showText(safeText);
            contentStream.endText();
        }

        private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
            List<String> lines = new ArrayList<>();
            StringBuilder currentLine = new StringBuilder();

            for (String word : safeText(text).split("\\s+")) {
                for (String fragment : splitWordToFit(word, font, fontSize, maxWidth)) {
                    String candidate = currentLine.isEmpty() ? fragment : currentLine + " " + fragment;
                    float width = stringWidth(candidate, font, fontSize);

                    if (width <= maxWidth) {
                        currentLine = new StringBuilder(candidate);
                    } else {
                        if (!currentLine.isEmpty()) {
                            lines.add(currentLine.toString());
                        }
                        currentLine = new StringBuilder(fragment);
                    }
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }

            return lines;
        }

        private List<String> splitWordToFit(String word, PDFont font, float fontSize, float maxWidth)
                throws IOException {
            if (word == null || word.isEmpty() || stringWidth(word, font, fontSize) <= maxWidth) {
                return List.of(word == null ? "" : word);
            }

            List<String> fragments = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (int offset = 0; offset < word.length(); ) {
                int codePoint = word.codePointAt(offset);
                String character = new String(Character.toChars(codePoint));
                String candidate = current + character;
                if (!current.isEmpty() && stringWidth(candidate, font, fontSize) > maxWidth) {
                    fragments.add(current.toString());
                    current = new StringBuilder(character);
                } else {
                    current.append(character);
                }
                offset += Character.charCount(codePoint);
            }
            if (!current.isEmpty()) {
                fragments.add(current.toString());
            }
            return fragments;
        }

        private float stringWidth(String text, PDFont font, float fontSize) throws IOException {
            return font.getStringWidth(sanitizeForFont(text, font)) / 1000F * fontSize;
        }

        private String sanitizeForFont(String text, PDFont font) throws IOException {
            if (text == null || text.isEmpty()) {
                return "";
            }

            StringBuilder sanitized = new StringBuilder();
            for (int offset = 0; offset < text.length(); ) {
                int codePoint = text.codePointAt(offset);
                String character = new String(Character.toChars(codePoint));
                try {
                    font.getStringWidth(character);
                    sanitized.append(character);
                } catch (IllegalArgumentException exception) {
                    sanitized.append('?');
                }
                offset += Character.charCount(codePoint);
            }
            return sanitized.toString();
        }

        private String safeText(String text) {
            return text == null ? "" : text.trim();
        }
    }
}
