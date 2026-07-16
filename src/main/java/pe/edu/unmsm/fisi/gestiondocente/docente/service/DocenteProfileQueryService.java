package pe.edu.unmsm.fisi.gestiondocente.docente.service;

import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.constancia.dto.response.CertificateGenerationResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.service.ConstanciaQueryService;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.ConstanciaPerfilResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.DocenteDto;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.DocentePerfilResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.entity.Docente;
import pe.edu.unmsm.fisi.gestiondocente.docente.exception.DocenteNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.docente.mapper.DocenteMapper;
import pe.edu.unmsm.fisi.gestiondocente.docente.repository.DocenteRepository;

@Service
public class DocenteProfileQueryService {

    private final DocenteRepository docenteRepository;
    private final DocenteMapper docenteMapper;
    private final ConstanciaQueryService constanciaQueryService;

    public DocenteProfileQueryService(DocenteRepository docenteRepository, DocenteMapper docenteMapper,
            ConstanciaQueryService constanciaQueryService) {
        this.docenteRepository = docenteRepository;
        this.docenteMapper = docenteMapper;
        this.constanciaQueryService = constanciaQueryService;
    }

    public DocentePerfilResponse obtenerPerfilDemo() {
        Docente docente = docenteRepository.findDemoDocente()
                .orElseThrow(() -> new DocenteNotFoundException("No se encontro el docente demo"));
        return construirPerfilDocente(docente);
    }

    public DocentePerfilResponse obtenerPerfilPorTeacherCode(String teacherCode) {
        Docente docente = docenteRepository.findByCodigo(teacherCode)
                .orElseThrow(() -> new DocenteNotFoundException("Docente no encontrado"));
        return construirPerfilDocente(docente);
    }

    private DocentePerfilResponse construirPerfilDocente(Docente docente) {
        DocenteDto docenteDto = docenteMapper.toDto(docente);
        List<ConstanciaPerfilResponse> constancias = constanciaQueryService
                .listLatestByTeacherCode(docente.getCodigo()).stream()
                .map(this::toPerfilResponse)
                .toList();

        return new DocentePerfilResponse(docenteDto, constancias);
    }

    private ConstanciaPerfilResponse toPerfilResponse(CertificateGenerationResponse constancia) {
        return new ConstanciaPerfilResponse(
                constancia.getGenerationId(),
                constancia.getCertificateKey(),
                constancia.getVersion(),
                constancia.getType(),
                constancia.getStatus(),
                constancia.getTeacherCode(),
                constancia.getCourseCode(),
                constancia.getSection(),
                constancia.getSemester(),
                constancia.getGeneratedAt(),
                constancia.getViewUrl(),
                constancia.getDownloadUrl());
    }
}
