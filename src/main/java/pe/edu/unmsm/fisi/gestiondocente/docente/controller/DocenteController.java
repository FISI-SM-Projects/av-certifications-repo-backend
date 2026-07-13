package pe.edu.unmsm.fisi.gestiondocente.docente.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.unmsm.fisi.gestiondocente.docente.dto.DocentePerfilResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.service.DocenteService;

@RestController
@RequestMapping("/api/v1/docentes")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    @GetMapping("/demo/perfil")
    public DocentePerfilResponse obtenerPerfilDocenteDemo() {
        return docenteService.obtenerPerfilDocenteDemo();
    }

    @GetMapping("/{teacherCode}/perfil")
    public DocentePerfilResponse obtenerPerfilDocentePorCodigo(@PathVariable String teacherCode) {
        return docenteService.obtenerPerfilDocentePorCodigo(teacherCode);
    }
}
