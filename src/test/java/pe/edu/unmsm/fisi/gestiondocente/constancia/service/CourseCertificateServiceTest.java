package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CoursePayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.IssuerPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.TeacherPayload;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CourseCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.ApprovedCertificateAlreadyExistsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.MissingRequiredFieldsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.PdfGenerationException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.StorageException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.pdf.PdfGenerationService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.CertificateGenerationRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.CourseCertificateRequestValidator;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.StoragePathSanitizer;

class CourseCertificateServiceTest {

    private CourseCertificateRequestValidator validator;
    private CertificateIdService certificateIdService;
    private CertificateGenerationRepository repository;
    private PdfGenerationService pdfGenerationService;
    private CourseCertificateService service;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        validator = mock(CourseCertificateRequestValidator.class);
        certificateIdService = new CertificateIdService(new StoragePathSanitizer());
        repository = mock(CertificateGenerationRepository.class);
        pdfGenerationService = mock(PdfGenerationService.class);
        fixedClock = Clock.fixed(Instant.parse("2026-07-14T15:30:00Z"), ZoneId.of("UTC"));
        service = new CourseCertificateService(validator, certificateIdService, repository, pdfGenerationService,
                fixedClock);
    }

    @Test
    void solicitudValidaDebeGenerarGuardarYResponder() {
        CourseCertificateRequest request = validRequest();
        when(repository.existsApprovedByCertificateKey("22200275-32BGNYGF-1-26.1")).thenReturn(false);
        when(repository.nextVersion("22200275-32BGNYGF-1-26.1")).thenReturn(1);
        when(pdfGenerationService.generateCourseCertificate(any(CourseCertificateRequest.class), any(CertificateGenerationMetadata.class)))
                .thenReturn(new byte[] { 1, 2, 3 });
        when(repository.saveGeneration(any(CourseCertificateRequest.class), any(CertificateGenerationMetadata.class), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        CourseCertificateResponse response = service.generateCourseCertificate(request);

        assertThat(response.getGenerationId()).isEqualTo("22200275-32BGNYGF-1-26.1-v001");
        assertThat(response.getCertificateKey()).isEqualTo("22200275-32BGNYGF-1-26.1");
        assertThat(response.getVersion()).isEqualTo(1);
        assertThat(response.getType()).isEqualTo(TipoConstancia.CURSO);
        assertThat(response.getStatus()).isEqualTo(EstadoConstancia.GENERADO);
        assertThat(response.getTeacherFullName()).isEqualTo("Jos\u00e9 Mu\u00f1oz Pe\u00f1a");
        assertThat(response.getCourseCode()).isEqualTo("32BGNYGF");
        assertThat(response.getCourseSubject()).isEqualTo("Nombre del curso");
        assertThat(response.getSection()).isEqualTo("1");
        assertThat(response.getSemester()).isEqualTo("26.1");
        assertThat(response.getGeneratedAt()).isEqualTo(LocalDateTime.of(2026, 7, 14, 15, 30));
        assertThat(response.getViewUrl()).isEqualTo(
                "/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/pdf");
        assertThat(response.getDownloadUrl()).isEqualTo(
                "/api/v1/constancias/generaciones/22200275-32BGNYGF-1-26.1-v001/download");

        ArgumentCaptor<CertificateGenerationMetadata> metadataCaptor =
                ArgumentCaptor.forClass(CertificateGenerationMetadata.class);
        verify(pdfGenerationService).generateCourseCertificate(any(CourseCertificateRequest.class), metadataCaptor.capture());
        CertificateGenerationMetadata metadata = metadataCaptor.getValue();
        assertThat(metadata.getGenerationId()).isEqualTo("22200275-32BGNYGF-1-26.1-v001");
        assertThat(metadata.getCertificateKey()).isEqualTo("22200275-32BGNYGF-1-26.1");
        assertThat(metadata.getVersion()).isEqualTo(1);
        assertThat(metadata.getType()).isEqualTo(TipoConstancia.CURSO);
        assertThat(metadata.getStatus()).isEqualTo(EstadoConstancia.GENERADO);
        assertThat(metadata.getTeacherCode()).isEqualTo("22200275");
        assertThat(metadata.getCourseCode()).isEqualTo("32BGNYGF");
        assertThat(metadata.getSection()).isEqualTo("1");
        assertThat(metadata.getSemester()).isEqualTo("26.1");
        assertThat(metadata.getRequestFile()).isEqualTo("request.json");
        assertThat(metadata.getPdfFile()).isEqualTo("certificate.pdf");
    }

    @Test
    void primeraVersionDebeUsarV001() {
        when(repository.nextVersion("22200275-32BGNYGF-1-26.1")).thenReturn(1);
        when(pdfGenerationService.generateCourseCertificate(any(), any())).thenReturn(new byte[] { 1 });
        when(repository.saveGeneration(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(1));

        CourseCertificateResponse response = service.generateCourseCertificate(validRequest());

        assertThat(response.getGenerationId()).endsWith("-v001");
        assertThat(response.getVersion()).isEqualTo(1);
    }

    @Test
    void regeneracionDebeUsarSiguienteVersion() {
        when(repository.nextVersion("22200275-32BGNYGF-1-26.1")).thenReturn(2);
        when(pdfGenerationService.generateCourseCertificate(any(), any())).thenReturn(new byte[] { 1 });
        when(repository.saveGeneration(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(1));

        CourseCertificateResponse response = service.generateCourseCertificate(validRequest());

        assertThat(response.getGenerationId()).endsWith("-v002");
        assertThat(response.getVersion()).isEqualTo(2);
    }

    @Test
    void constanciaAprobadaDebeBloquearGeneracion() {
        when(repository.existsApprovedByCertificateKey("22200275-32BGNYGF-1-26.1")).thenReturn(true);

        assertThatThrownBy(() -> service.generateCourseCertificate(validRequest()))
                .isInstanceOf(ApprovedCertificateAlreadyExistsException.class);

        verify(pdfGenerationService, never()).generateCourseCertificate(any(), any());
        verify(repository, never()).saveGeneration(any(), any(), any());
    }

    @Test
    void validacionFallidaNoDebeGenerarPdfNiGuardar() {
        CourseCertificateRequest request = validRequest();
        doThrow(new MissingRequiredFieldsException(List.of("teacher.email"))).when(validator).validate(any());

        assertThatThrownBy(() -> service.generateCourseCertificate(request))
                .isInstanceOf(MissingRequiredFieldsException.class);

        verify(repository, never()).existsApprovedByCertificateKey(any());
        verify(pdfGenerationService, never()).generateCourseCertificate(any(), any());
        verify(repository, never()).saveGeneration(any(), any(), any());
    }

    @Test
    void errorPdfNoDebeGuardarGeneracion() {
        when(repository.nextVersion("22200275-32BGNYGF-1-26.1")).thenReturn(1);
        when(pdfGenerationService.generateCourseCertificate(any(), any()))
                .thenThrow(new PdfGenerationException("No se pudo generar"));

        assertThatThrownBy(() -> service.generateCourseCertificate(validRequest()))
                .isInstanceOf(PdfGenerationException.class);

        verify(repository, never()).saveGeneration(any(), any(), any());
    }

    @Test
    void errorStorageNoDebeDevolverExito() {
        when(repository.nextVersion("22200275-32BGNYGF-1-26.1")).thenReturn(1);
        when(pdfGenerationService.generateCourseCertificate(any(), any())).thenReturn(new byte[] { 1 });
        when(repository.saveGeneration(any(), any(), any())).thenThrow(new StorageException("fallo"));

        assertThatThrownBy(() -> service.generateCourseCertificate(validRequest()))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void debeUsarFechaFijaDelClock() {
        when(repository.nextVersion("22200275-32BGNYGF-1-26.1")).thenReturn(1);
        when(pdfGenerationService.generateCourseCertificate(any(), any())).thenReturn(new byte[] { 1 });
        when(repository.saveGeneration(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(1));

        CourseCertificateResponse response = service.generateCourseCertificate(validRequest());

        assertThat(response.getGeneratedAt()).isEqualTo(LocalDateTime.of(2026, 7, 14, 15, 30));
    }

    private CourseCertificateRequest validRequest() {
        return new CourseCertificateRequest(
                new TeacherPayload("Jos\u00e9 Mu\u00f1oz Pe\u00f1a", "jmunoz@unmsm.edu.pe", "22200275"),
                new CoursePayload("32BGNYGF", "Nombre del curso", "7", "1", "SW", "2023", "26.1"),
                new IssuerPayload("moodle", "12345", "usuario@unmsm.edu.pe"));
    }
}
