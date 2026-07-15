package pe.edu.unmsm.fisi.gestiondocente.constancia.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class CertificateGenerationMetadataTest {

    @Test
    void debePermitirMetadataDeCurso() {
        LocalDateTime generatedAt = LocalDateTime.of(2026, 7, 14, 10, 30);

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
                "constancia.pdf");

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
}
