package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CertificateIdServiceTest {

    private final CertificateIdService certificateIdService = new CertificateIdService();

    @Test
    void debeConstruirClavePorCurso() {
        String key = certificateIdService.buildCourseCertificateKey("22200275", "32BGNYGF", "1", "26.1");

        assertThat(key).isEqualTo("22200275-32BGNYGF-1-26.1");
    }

    @Test
    void debeConstruirClaveSemestral() {
        String key = certificateIdService.buildSemesterCertificateKey("22200275", "26.1");

        assertThat(key).isEqualTo("22200275-26.1");
    }

    @Test
    void debeConstruirGenerationIdV001() {
        String generationId = certificateIdService.buildGenerationId("22200275-32BGNYGF-1-26.1", 1);

        assertThat(generationId).isEqualTo("22200275-32BGNYGF-1-26.1-v001");
    }

    @Test
    void debeConstruirGenerationIdV012() {
        String generationId = certificateIdService.buildGenerationId("22200275-32BGNYGF-1-26.1", 12);

        assertThat(generationId).isEqualTo("22200275-32BGNYGF-1-26.1-v012");
    }

    @Test
    void debeConstruirGenerationIdV123() {
        String generationId = certificateIdService.buildGenerationId("22200275-32BGNYGF-1-26.1", 123);

        assertThat(generationId).isEqualTo("22200275-32BGNYGF-1-26.1-v123");
    }

    @Test
    void debeRechazarVersionCero() {
        assertThatThrownBy(() -> certificateIdService.buildGenerationId("22200275-26.1", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La version debe ser mayor o igual a 1");
    }

    @Test
    void debeRechazarVersionNegativa() {
        assertThatThrownBy(() -> certificateIdService.buildGenerationId("22200275-26.1", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La version debe ser mayor o igual a 1");
    }
}
