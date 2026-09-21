package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.CertificateGenerationNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.CertificateGenerationRepository;

@ExtendWith(MockitoExtension.class)
class ConstanciaQueryServiceTest {

    @Mock
    private CertificateGenerationRepository repository;

    @InjectMocks
    private ConstanciaQueryService service;

    @Test
    void hidesGenerationOwnedByAnotherTeacher() {
        CertificateGenerationMetadata metadata = new CertificateGenerationMetadata(
                "generation-1",
                "certificate-1",
                1,
                TipoConstancia.CURSO,
                EstadoConstancia.GENERADO,
                "OTHER001",
                "COURSE01",
                "1",
                "2026-1",
                Instant.parse("2026-01-01T00:00:00Z"),
                "request.json",
                "certificate.pdf");
        when(repository.findByGenerationId("generation-1")).thenReturn(Optional.of(metadata));

        assertThatThrownBy(() -> service.findAuthenticatedTeacherGeneration("generation-1", "DEV001"))
                .isInstanceOf(CertificateGenerationNotFoundException.class);
    }
}
