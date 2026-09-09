package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.unmsm.fisi.gestiondocente.auth.entity.*;
import pe.edu.unmsm.fisi.gestiondocente.auth.repository.InstitutionalAccountRepository;
import pe.edu.unmsm.fisi.gestiondocente.auth.service.CurrentAccountService;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Teacher;
import pe.edu.unmsm.fisi.gestiondocente.curso.entity.Course;
import pe.edu.unmsm.fisi.gestiondocente.periodo.entity.AcademicPeriod;
import pe.edu.unmsm.fisi.gestiondocente.cargadocente.entity.*;
import pe.edu.unmsm.fisi.gestiondocente.cargadocente.repository.AcademicWorkloadRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.*;
import pe.edu.unmsm.fisi.gestiondocente.constancia.repository.CertificationRepository;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.pdf.PdfGenerationService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class InstitutionalCertificateServiceTest {
    @TempDir Path root;
    CertificationRepository repo = mock(CertificationRepository.class);
    AcademicWorkloadRepository workloads = mock(AcademicWorkloadRepository.class);
    CurrentAccountService identity = mock(CurrentAccountService.class);
    InstitutionalAccountRepository accounts = mock(InstitutionalAccountRepository.class);
    PdfGenerationService pdf = mock(PdfGenerationService.class);
    Authentication auth = mock(Authentication.class);
    InstitutionalPdfStorage storage;
    InstitutionalCertificateService service;
    AcademicWorkload workload;
    List<Certification> rows = new ArrayList<>();
    @BeforeEach void setup() {
        storage = new InstitutionalPdfStorage(root.toString());
        service = new InstitutionalCertificateService(repo, workloads, identity, accounts, storage, pdf);
        var person = mock(Person.class); when(person.getId()).thenReturn(1L); when(person.getFullName()).thenReturn("Nombre Real");
        var teacher = new Teacher(); teacher.setId(1L); teacher.setPerson(person); teacher.setCode("00112233");
        var course = new Course(); course.setCode("202W0701"); course.setName("Curso Real");
        var period = new AcademicPeriod(); period.setSemesterCode("26.1");
        workload = new AcademicWorkload(); workload.setId(5L); workload.setTeacher(teacher); workload.setCourse(course);
        workload.setAcademicPeriod(period); workload.setCycle(8); workload.setSection(1); workload.setSchool(School.SW); workload.setPlan(2018);
        var account = mock(InstitutionalAccount.class);
        when(account.getId()).thenReturn(1L); when(account.getAccountStatus()).thenReturn(AccountStatus.ACTIVO);
        when(account.getInstitutionalEmail()).thenReturn("real@unmsm.edu.pe"); when(identity.account(auth)).thenReturn(account);
        when(accounts.findByPersonIdOrderByMainDescIdAsc(1L)).thenReturn(List.of(account));
        when(workloads.findLockedById(5L)).thenReturn(Optional.of(workload));
        when(repo.findByAcademicWorkloadIdOrderById(5L)).thenAnswer(i -> List.copyOf(rows));
        when(repo.saveAndFlush(any())).thenAnswer(i -> { Certification c = i.getArgument(0); c.setId(7L); rows.add(c); return c; });
        when(pdf.generateCourseCertificate(any(), any())).thenReturn("%PDF-1.7 test".getBytes());
        TransactionSynchronizationManager.initSynchronization();
    }
    @AfterEach void clear() { TransactionSynchronizationManager.clearSynchronization(); }
    @Test void generationUsesRealWorkloadAndRegistersDocumentPath() {
        var result = service.generate(new InstitutionalCertificateService.GenerateRequest(5L, null, null, null), auth);
        assertEquals("7", result.generationId()); assertEquals("00112233", result.teacherCode());
        assertEquals("EMITIDO", result.status()); assertTrue(result.pdfAvailable());
        assertEquals("workload-5", result.certificateKey()); assertEquals(1, result.version());
        assertTrue(storage.available(rows.getFirst().getDocumentPath()));
    }
    @Test void failedTransactionCleansOnlyItsPdf() {
        service.generate(new InstitutionalCertificateService.GenerateRequest(5L, null, null, null), auth);
        var path = rows.getFirst().getDocumentPath();
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        assertFalse(storage.available(path));
    }
    @Test void invalidPdfFailsControlled() {
        when(pdf.generateCourseCertificate(any(), any())).thenReturn(new byte[0]);
        assertThrows(ResponseStatusException.class, () -> service.generate(new InstitutionalCertificateService.GenerateRequest(5L, null, null, null), auth));
        assertFalse(storage.available(rows.getFirst().getDocumentPath()));
    }
    @Test void verifiedWorkloadRejectsGeneration() {
        var verified = new Certification(); verified.setStatus(CertificationStatus.VERIFICADO); rows.add(verified);
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.generate(new InstitutionalCertificateService.GenerateRequest(5L, null, null, null), auth)).getStatusCode().value());
        verify(repo, never()).saveAndFlush(any()); verifyNoInteractions(pdf);
    }
    @Test void missingWorkloadDoesNotPersist() {
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.generate(new InstitutionalCertificateService.GenerateRequest(99L, null, null, null), auth)).getStatusCode().value());
        verify(repo, never()).saveAndFlush(any());
    }
    @Test void listingUsesDatabaseStatusAndLimaTimestamp() {
        var cert = new Certification(); cert.setId(2L); cert.setAcademicWorkload(workload);
        cert.setStatus(CertificationStatus.REVOCADO); cert.setCreatedAt(LocalDateTime.of(2026, 9, 8, 10, 0));
        cert.setDocumentPath("missing.pdf"); rows.add(cert);
        when(repo.findByTeacherCodeOrderByIdDesc("00112233")).thenReturn(List.of(cert));
        var item = service.list("00112233", auth).getFirst();
        assertEquals("REVOCADO", item.status()); assertEquals("2026-09-08T15:00:00Z", item.generatedAt().toString());
        assertFalse(item.pdfAvailable());
    }
}
