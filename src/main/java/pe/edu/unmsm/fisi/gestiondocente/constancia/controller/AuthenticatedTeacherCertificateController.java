package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.unmsm.fisi.gestiondocente.auth.dto.UserPrincipal;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.AuthenticatedTeacherContext;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.AuthenticatedCourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.AuthenticatedSemesterCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CertificateGenerationResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CourseCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.SemesterCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.AuthenticatedTeacherContextService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.ConstanciaQueryService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.CourseCertificateService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.SemesterCertificateService;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.DefaultResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teachers/me/constancias")
@PreAuthorize("hasRole('DOCENTE')")
public class AuthenticatedTeacherCertificateController {

    private final AuthenticatedTeacherContextService teacherContextService;
    private final ConstanciaQueryService constanciaQueryService;
    private final CourseCertificateService courseCertificateService;
    private final SemesterCertificateService semesterCertificateService;

    @GetMapping
    public ResponseEntity<DefaultResponse<List<CertificateGenerationResponse>>> list(
            @AuthenticationPrincipal UserPrincipal principal) {
        AuthenticatedTeacherContext teacher = teacherContextService.getContext(principal);
        List<CertificateGenerationResponse> certificates =
                constanciaQueryService.listAuthenticatedTeacherCertificates(teacher.teacherCode());
        return ResponseEntity.ok(DefaultResponse.success("Constancias del docente autenticado", certificates));
    }

    @PostMapping("/curso")
    @ResponseStatus(HttpStatus.CREATED)
    public DefaultResponse<CourseCertificateResponse> generateCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) AuthenticatedCourseCertificateRequest request) {
        AuthenticatedTeacherContext teacher = teacherContextService.getContext(principal);
        return DefaultResponse.success(
                "Constancia por curso generada",
                courseCertificateService.generateAuthenticatedCourseCertificate(request, teacher));
    }

    @PostMapping("/semestral")
    @ResponseStatus(HttpStatus.CREATED)
    public DefaultResponse<SemesterCertificateResponse> generateSemester(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) AuthenticatedSemesterCertificateRequest request) {
        AuthenticatedTeacherContext teacher = teacherContextService.getContext(principal);
        return DefaultResponse.success(
                "Constancia semestral generada",
                semesterCertificateService.generateAuthenticatedSemesterCertificate(request, teacher));
    }

    @GetMapping("/generaciones/{generationId}")
    public ResponseEntity<DefaultResponse<CertificateGenerationResponse>> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String generationId) {
        AuthenticatedTeacherContext teacher = teacherContextService.getContext(principal);
        CertificateGenerationResponse certificate = constanciaQueryService
                .findAuthenticatedTeacherGeneration(generationId, teacher.teacherCode());
        return ResponseEntity.ok(DefaultResponse.success("Detalle de constancia", certificate));
    }

    @GetMapping("/certificados/{certificateKey}/historial")
    public ResponseEntity<DefaultResponse<List<CertificateGenerationResponse>>> history(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String certificateKey) {
        AuthenticatedTeacherContext teacher = teacherContextService.getContext(principal);
        List<CertificateGenerationResponse> history = constanciaQueryService
                .findAuthenticatedTeacherHistory(certificateKey, teacher.teacherCode());
        return ResponseEntity.ok(DefaultResponse.success("Historial de constancia", history));
    }

    @GetMapping("/generaciones/{generationId}/pdf")
    public ResponseEntity<byte[]> viewPdf(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String generationId) {
        return pdfResponse(readOwnedPdf(principal, generationId), generationId, false);
    }

    @GetMapping("/generaciones/{generationId}/download")
    public ResponseEntity<byte[]> downloadPdf(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String generationId) {
        return pdfResponse(readOwnedPdf(principal, generationId), generationId, true);
    }

    private byte[] readOwnedPdf(UserPrincipal principal, String generationId) {
        AuthenticatedTeacherContext teacher = teacherContextService.getContext(principal);
        return constanciaQueryService.readAuthenticatedTeacherPdf(generationId, teacher.teacherCode());
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdfBytes, String generationId, boolean attachment) {
        ContentDisposition contentDisposition = attachment
                ? ContentDisposition.attachment().filename(generationId + ".pdf").build()
                : ContentDisposition.inline().filename(generationId + ".pdf").build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(pdfBytes);
    }
}
