package pe.edu.unmsm.fisi.gestiondocente.constancia.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request.CourseCertificateRequest;
import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CourseCertificateResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.CourseCertificateService;

@RestController
@RequestMapping("/api/v1/constancias")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR_ESCUELA')")
public class CourseCertificateController {

    private final CourseCertificateService courseCertificateService;

    public CourseCertificateController(CourseCertificateService courseCertificateService) {
        this.courseCertificateService = courseCertificateService;
    }

    @PostMapping("/curso")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseCertificateResponse generateCourseCertificate(
            @RequestBody(required = false) CourseCertificateRequest request) {
        return courseCertificateService.generateCourseCertificate(request);
    }
}
