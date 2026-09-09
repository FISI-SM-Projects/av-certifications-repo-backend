package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.InstitutionalCertificateService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.InstitutionalCertificateResponse;
import java.util.List;

@RestController
@Profile("!demo & !test")
@RequestMapping("/api/v1/constancias")
public class InstitutionalCertificateController {
    private final InstitutionalCertificateService service;
    public InstitutionalCertificateController(InstitutionalCertificateService service) { this.service = service; }
    @GetMapping("/docentes/{teacherCode}")
    public List<InstitutionalCertificateResponse> list(@PathVariable String teacherCode, Authentication auth) { return service.list(teacherCode, auth); }
    @GetMapping("/generaciones/{generationId}")
    public InstitutionalCertificateResponse detail(@PathVariable String generationId, Authentication auth) { return service.detail(generationId, auth); }
    @GetMapping("/certificados/{certificateKey}/historial")
    public List<InstitutionalCertificateResponse> history(@PathVariable String certificateKey, Authentication auth) { return service.history(certificateKey, auth); }
    @GetMapping("/generaciones/{generationId}/pdf")
    public ResponseEntity<byte[]> view(@PathVariable String generationId, Authentication auth) { return document(generationId, auth, false); }
    @GetMapping("/generaciones/{generationId}/download")
    public ResponseEntity<byte[]> download(@PathVariable String generationId, Authentication auth) { return document(generationId, auth, true); }
    private ResponseEntity<byte[]> document(String id, Authentication auth, boolean attachment) {
        byte[] bytes = service.readPdf(id, auth);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, (attachment ? ContentDisposition.attachment() : ContentDisposition.inline()).filename(id + ".pdf").build().toString()).body(bytes);
    }
    @PostMapping("/curso")
    @ResponseStatus(HttpStatus.CREATED)
    public InstitutionalCertificateResponse generate(@RequestBody InstitutionalCertificateService.GenerateRequest request, Authentication auth) {
        return service.generate(request, auth);
    }
    @PostMapping("/semestral")
    public void semester() {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "La consolidacion semestral requiere soporte en el modelo institucional; disponible solo en demo");
    }
}
