package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.api.CertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.api.CreateCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.InstitutionalCertificateService;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.DefaultResponse;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.PaginatedResponse;

@RestController
@Profile("!test")
@RequestMapping("/api/v1/certificates")
public class CertificateApiController {
    private static final String SUCCESS_MESSAGE = "Operación completada exitosamente";
    private final InstitutionalCertificateService service;

    public CertificateApiController(InstitutionalCertificateService service) {
        this.service = service;
    }

    @GetMapping
    public PaginatedResponse<List<CertificateResponse>> list(
            @RequestParam(required = false) String teacherCode,
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication) {
        var result = service.listApi(teacherCode, certificateType, status, semester, course, page, size, authentication);
        return PaginatedResponse.success(SUCCESS_MESSAGE, result.data(), result.pagination());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DefaultResponse<CertificateResponse> create(@RequestBody CreateCertificateRequest request,
            Authentication authentication) {
        var response = service.createApi(request, authentication);
        String message = "Operación completada exitosamente";
        return DefaultResponse.success(message, response);
    }

    @GetMapping("/{id}")
    public DefaultResponse<CertificateResponse> detail(@PathVariable String id, Authentication authentication) {
        return DefaultResponse.success(SUCCESS_MESSAGE, service.detailApi(id, authentication));
    }

    @GetMapping("/{id}/document")
    public ResponseEntity<byte[]> document(
            @PathVariable String id,
            @RequestParam(defaultValue = "inline") String disposition,
            Authentication authentication) {
        boolean attachment = switch (disposition.toLowerCase()) {
            case "inline" -> false;
            case "attachment" -> true;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "disposition debe ser inline o attachment");
        };
        byte[] bytes = service.readPdf(id, authentication);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        (attachment ? ContentDisposition.attachment() : ContentDisposition.inline())
                                .filename(id + ".pdf")
                                .build()
                                .toString())
                .body(bytes);
    }

    @GetMapping("/{id}/versions")
    public PaginatedResponse<List<CertificateResponse>> versions(
            @PathVariable String id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication) {
        var result = service.versionsApi(id, page, size, authentication);
        return PaginatedResponse.success(SUCCESS_MESSAGE, result.data(), result.pagination());
    }

    @PostMapping("/{id}/signature")
    public DefaultResponse<CertificateResponse> signature(@PathVariable String id, Authentication authentication) {
        return DefaultResponse.success(SUCCESS_MESSAGE, service.signApi(id, authentication));
    }
}
