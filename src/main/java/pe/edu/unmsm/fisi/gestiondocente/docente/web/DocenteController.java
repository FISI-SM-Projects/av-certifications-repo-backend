package pe.edu.unmsm.fisi.gestiondocente.docente.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.unmsm.fisi.gestiondocente.docente.web.dto.DocentePerfilResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.application.DocenteProfileQueryService;
import pe.edu.unmsm.fisi.gestiondocente.docente.application.DocenteService;

@RestController
@RequestMapping("/api/v1/docentes")
public class DocenteController {

    private final DocenteService docenteService;
    private final DocenteProfileQueryService docenteProfileQueryService;

    public DocenteController(DocenteService docenteService, DocenteProfileQueryService docenteProfileQueryService) {
        this.docenteService = docenteService;
        this.docenteProfileQueryService = docenteProfileQueryService;
    }

    @GetMapping("/demo/perfil")
    public DocentePerfilResponse obtenerPerfilDocenteDemo() {
        return docenteProfileQueryService.obtenerPerfilDemo();
    }

    @GetMapping("/{teacherCode}/perfil")
    public DocentePerfilResponse obtenerPerfilDocentePorCodigo(@PathVariable String teacherCode) {
        return docenteProfileQueryService.obtenerPerfilPorTeacherCode(teacherCode);
    }
}
