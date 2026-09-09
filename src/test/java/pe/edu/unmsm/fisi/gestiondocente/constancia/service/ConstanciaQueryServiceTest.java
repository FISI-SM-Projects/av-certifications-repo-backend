package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CertificateGenerationResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.CertificateGenerationNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.CertificatePdfNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.CertificateGenerationRepository;

class ConstanciaQueryServiceTest {

    private CertificateGenerationRepository repository;
    private ConstanciaQueryService service;

    @BeforeEach
    void setUp() {
        repository = mock(CertificateGenerationRepository.class);
        service = new ConstanciaQueryService(repository);
    }

    @Test
    void debeListarUltimasConstanciasPorDocente() {
        when(repository.findLatestByTeacherCode("22200275")).thenReturn(List.of(metadata(1), metadata(2)));

        List<CertificateGenerationResponse> result = service.listLatestByTeacherCode("22200275");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CertificateGenerationResponse::getGenerationId)
                .containsExactly("22200275-32BGNYGF-1-26.1-v001", "22200275-32BGNYGF-1-26.1-v002");
        assertThat(result.get(0).getViewUrl()).isEqualTo(
                "/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/pdf");
        assertThat(result.get(0).getDownloadUrl()).isEqualTo(
                "/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/download");
    }

    @Test
    void debeConsultarGeneracionConcreta() {
        when(repository.findByGenerationId("22200275-32BGNYGF-1-26.1-v001"))
                .thenReturn(Optional.of(metadata(1)));

        CertificateGenerationResponse response = service.findByGenerationId("22200275-32BGNYGF-1-26.1-v001");

        assertThat(response.getCertificateKey()).isEqualTo("22200275-32BGNYGF-1-26.1");
        assertThat(response.getVersion()).isEqualTo(1);
        assertThat(response.getType()).isEqualTo(TipoConstancia.CURSO);
        assertThat(response.getStatus()).isEqualTo(EstadoConstancia.GENERADO);
        assertThat(response.getTeacherCode()).isEqualTo("22200275");
    }

    @Test
    void generacionInexistenteDebeLanzarExcepcion() {
        when(repository.findByGenerationId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByGenerationId("inexistente"))
                .isInstanceOf(CertificateGenerationNotFoundException.class)
                .hasMessage("Generación de constancia no encontrada");
    }

    @Test
    void debeConsultarHistorialPorClaveLogica() {
        when(repository.findHistoryByCertificateKey("22200275-32BGNYGF-1-26.1"))
                .thenReturn(List.of(metadata(1), metadata(2)));

        List<CertificateGenerationResponse> history =
                service.findHistoryByCertificateKey("22200275-32BGNYGF-1-26.1");

        assertThat(history).extracting(CertificateGenerationResponse::getVersion)
                .containsExactly(1, 2);
    }

    @Test
    void debeLeerPdfExistente() {
        when(repository.findByGenerationId("22200275-32BGNYGF-1-26.1-v001"))
                .thenReturn(Optional.of(metadata(1)));
        when(repository.readPdf("22200275-32BGNYGF-1-26.1-v001"))
                .thenReturn(Optional.of(new byte[] { 1, 2, 3 }));

        byte[] pdf = service.readPdf("22200275-32BGNYGF-1-26.1-v001");

        assertThat(pdf).containsExactly(1, 2, 3);
    }

    @Test
    void pdfInexistenteDebeLanzarExcepcion() {
        when(repository.findByGenerationId("22200275-32BGNYGF-1-26.1-v001"))
                .thenReturn(Optional.of(metadata(1)));
        when(repository.readPdf("22200275-32BGNYGF-1-26.1-v001"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.readPdf("22200275-32BGNYGF-1-26.1-v001"))
                .isInstanceOf(CertificatePdfNotFoundException.class)
                .hasMessage("PDF de constancia no encontrado");
    }

    private CertificateGenerationMetadata metadata(int version) {
        String certificateKey = "22200275-32BGNYGF-1-26.1";
        return new CertificateGenerationMetadata(
                certificateKey + "-v" + String.format("%03d", version),
                certificateKey,
                version,
                TipoConstancia.CURSO,
                EstadoConstancia.GENERADO,
                "22200275",
                "32BGNYGF",
                "1",
                "26.1",
                Instant.parse("2026-07-14T10:30:00Z"),
                "request.json",
                "certificate.pdf");
    }
}
