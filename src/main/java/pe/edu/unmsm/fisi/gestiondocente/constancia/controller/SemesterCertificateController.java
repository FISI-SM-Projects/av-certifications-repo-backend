package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.SemesterCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.SemesterCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.SemesterCertificateService;

@RestController
@RequestMapping("/api/v1/constancias")
public class SemesterCertificateController {

    private final SemesterCertificateService semesterCertificateService;

    public SemesterCertificateController(SemesterCertificateService semesterCertificateService) {
        this.semesterCertificateService = semesterCertificateService;
    }

    @PostMapping("/semestral")
    @ResponseStatus(HttpStatus.CREATED)
    public SemesterCertificateResponse generateSemesterCertificate(
            @RequestBody(required = false) SemesterCertificateRequest request) {
        return semesterCertificateService.generateSemesterCertificate(request);
    }
}
