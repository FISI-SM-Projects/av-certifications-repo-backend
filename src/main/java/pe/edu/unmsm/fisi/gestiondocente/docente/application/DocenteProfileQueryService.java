package pe.edu.unmsm.fisi.gestiondocente.docente.application;

import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.unmsm.fisi.gestiondocente.constancia.web.dto.response.CertificateGenerationResponse;
import pe.edu.unmsm.fisi.gestiondocente.constancia.application.ConstanciaQueryService;
import pe.edu.unmsm.fisi.gestiondocente.docente.web.dto.ConstanciaPerfilResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.web.dto.DocenteDto;
import pe.edu.unmsm.fisi.gestiondocente.docente.web.dto.DocentePerfilResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.domain.Docente;
import pe.edu.unmsm.fisi.gestiondocente.docente.domain.exception.DocenteNotFoundException;
import pe.edu.unmsm.fisi.gestiondocente.docente.application.mapper.DocenteMapper;
import pe.edu.unmsm.fisi.gestiondocente.docente.infrastructure.repository.DocenteRepository;

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
