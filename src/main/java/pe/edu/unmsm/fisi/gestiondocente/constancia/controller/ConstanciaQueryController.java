package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CertificateGenerationResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.ConstanciaQueryService;

@RestController
@RequestMapping("/api/v1/constancias")
public class ConstanciaQueryController {

    private final ConstanciaQueryService constanciaQueryService;

    public ConstanciaQueryController(ConstanciaQueryService constanciaQueryService) {
        this.constanciaQueryService = constanciaQueryService;
    }

    @GetMapping("/docentes/{teacherCode}")
    public List<CertificateGenerationResponse> listLatestByTeacherCode(@PathVariable String teacherCode) {
        return constanciaQueryService.listLatestByTeacherCode(teacherCode);
    }

    @GetMapping("/generaciones/{generationId}")
    public CertificateGenerationResponse findByGenerationId(@PathVariable String generationId) {
        return constanciaQueryService.findByGenerationId(generationId);
    }

    @GetMapping("/certificados/{certificateKey}/historial")
    public List<CertificateGenerationResponse> findHistoryByCertificateKey(@PathVariable String certificateKey) {
        return constanciaQueryService.findHistoryByCertificateKey(certificateKey);
    }

    @GetMapping("/generaciones/{generationId}/pdf")
    public ResponseEntity<byte[]> viewPdf(@PathVariable String generationId) {
        byte[] pdfBytes = constanciaQueryService.readPdf(generationId);
        return pdfResponse(pdfBytes, generationId, false);
    }

    @GetMapping("/generaciones/{generationId}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String generationId) {
        byte[] pdfBytes = constanciaQueryService.readPdf(generationId);
        return pdfResponse(pdfBytes, generationId, true);
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
