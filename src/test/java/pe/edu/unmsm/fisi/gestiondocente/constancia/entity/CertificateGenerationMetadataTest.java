package pe.edu.unmsm.fisi.gestiondocente.constancia.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class CertificateGenerationMetadataTest {

    @Test
    void debePermitirMetadataDeCurso() {
        Instant generatedAt = Instant.parse("2026-07-14T10:30:00Z");

        CertificateGenerationMetadata metadata = new CertificateGenerationMetadata(
                "22200275-32BGNYGF-1-26.1-v001",
                "22200275-32BGNYGF-1-26.1",
                1,
                TipoConstancia.CURSO,
                EstadoConstancia.GENERADO,
                "22200275",
                "32BGNYGF",
                "1",
                "26.1",
                generatedAt,
                "request.json",
                "certificate.pdf");

        assertThat(metadata.getGenerationId()).isEqualTo("22200275-32BGNYGF-1-26.1-v001");
        assertThat(metadata.getVersion()).isEqualTo(1);
        assertThat(metadata.getType()).isEqualTo(TipoConstancia.CURSO);
        assertThat(metadata.getStatus()).isEqualTo(EstadoConstancia.GENERADO);
        assertThat(metadata.getGeneratedAt()).isEqualTo(generatedAt);
    }

    @Test
    void debePermitirCourseCodeYSectionNulosParaMetadataSemestral() {
        CertificateGenerationMetadata metadata = new CertificateGenerationMetadata();

        metadata.setType(TipoConstancia.SEMESTRAL);
        metadata.setStatus(EstadoConstancia.GENERADO);
        metadata.setTeacherCode("22200275");
        metadata.setSemester("26.1");
        metadata.setCourseCode(null);
        metadata.setSection(null);

        assertThat(metadata.getType()).isEqualTo(TipoConstancia.SEMESTRAL);
        assertThat(metadata.getCourseCode()).isNull();
        assertThat(metadata.getSection()).isNull();
    }

    @Test
    void debeSerializarGeneratedAtConZonaExplicita() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        CertificateGenerationMetadata metadata = new CertificateGenerationMetadata(
                "22200275-32BGNYGF-1-26.1-v001",
                "22200275-32BGNYGF-1-26.1",
                1,
                TipoConstancia.CURSO,
                EstadoConstancia.GENERADO,
                "22200275",
                "32BGNYGF",
                "1",
                "26.1",
                Instant.parse("2026-07-14T10:30:00Z"),
                "request.json",
                "certificate.pdf");

        String json = objectMapper.writeValueAsString(metadata);

        assertThat(json).contains("\"generatedAt\":\"2026-07-14T10:30:00Z\"");
    }

    @Test
    void debeLeerMetadataLegacySinZonaComoAmericaLima() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json = """
                {
                  "generationId": "22200275-32BGNYGF-1-26.1-v001",
                  "certificateKey": "22200275-32BGNYGF-1-26.1",
                  "version": 1,
                  "type": "CURSO",
                  "status": "GENERADO",
                  "teacherCode": "22200275",
                  "courseCode": "32BGNYGF",
                  "section": "1",
                  "semester": "26.1",
                  "generatedAt": "2026-07-14T10:30:00",
                  "requestFile": "request.json",
                  "pdfFile": "certificate.pdf"
                }
                """;

        CertificateGenerationMetadata metadata = objectMapper.readValue(json, CertificateGenerationMetadata.class);

        assertThat(metadata.getGeneratedAt()).isEqualTo(Instant.parse("2026-07-14T15:30:00Z"));
    }
}
