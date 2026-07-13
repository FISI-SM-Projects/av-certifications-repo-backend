package pe.edu.unmsm.fisi.gestiondocente.docente.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.unmsm.fisi.gestiondocente.docente.dto.DocenteListadoDto;
import pe.edu.unmsm.fisi.gestiondocente.docente.service.DocenteService;

@RestController
@RequestMapping("/api/v1/director/docentes")
public class DirectorDocenteController {

    private final DocenteService docenteService;

    public DirectorDocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    @GetMapping
    public List<DocenteListadoDto> listarDocentesPorDepartamento(
            @RequestParam(required = false) String departamentoAcademico) {
        return docenteService.listarDocentesPorDepartamento(departamentoAcademico);
    }
}
