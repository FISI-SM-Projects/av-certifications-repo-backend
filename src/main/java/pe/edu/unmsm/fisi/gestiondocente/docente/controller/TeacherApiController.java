package pe.edu.unmsm.fisi.gestiondocente.docente.controller;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.api.TeacherCourseResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.dto.api.TeacherMeResponse;
import pe.edu.unmsm.fisi.gestiondocente.docente.service.TeacherApiService;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.DefaultResponse;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.PaginatedResponse;

@RestController
@Profile("!test")
@RequestMapping("/api/v1/teachers")
public class TeacherApiController {
    private static final String SUCCESS_MESSAGE = "Operación completada exitosamente";

    private final TeacherApiService service;

    public TeacherApiController(TeacherApiService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public DefaultResponse<TeacherMeResponse> me(Authentication authentication) {
        return DefaultResponse.success(SUCCESS_MESSAGE, service.me(authentication));
    }

    @GetMapping("/me/courses")
    public PaginatedResponse<List<TeacherCourseResponse>> courses(
            Authentication authentication,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer cycle,
            @RequestParam(required = false) Integer plan,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        TeacherApiService.CoursePage result = service.courses(authentication, semester, cycle, plan, course, page, size);
        return PaginatedResponse.success(SUCCESS_MESSAGE, result.data(), result.pagination());
    }
}
