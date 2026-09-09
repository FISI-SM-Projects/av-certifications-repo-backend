package pe.edu.unmsm.fisi.gestiondocente.docente.controller;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.unmsm.fisi.gestiondocente.docente.service.InstitutionalTeacherService;

@RestController
@Profile("!test")
@RequestMapping("/api/v1")
public class InstitutionalTeacherController {
    private final InstitutionalTeacherService service;
    public InstitutionalTeacherController(InstitutionalTeacherService service) { this.service = service; }
    @GetMapping("/docentes/{teacherCode}/perfil")
    public InstitutionalTeacherService.Profile profile(@PathVariable String teacherCode, Authentication auth) { return service.profile(teacherCode, auth); }
    @GetMapping("/docentes/{teacherCode}/carga-academica")
    public List<InstitutionalTeacherService.Workload> workload(@PathVariable String teacherCode, Authentication auth) { return service.workload(teacherCode, auth); }
    @GetMapping("/director/docentes")
    public List<InstitutionalTeacherService.TeacherData> directory(@RequestParam(required = false) String departamentoAcademico, Authentication auth) { return service.directory(departamentoAcademico, auth); }
}
