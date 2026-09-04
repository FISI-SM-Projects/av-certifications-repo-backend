package pe.edu.unmsm.fisi.gestiondocente.teacher.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unmsm.fisi.gestiondocente.auth.dto.UserPrincipal;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.DefaultResponse;
import pe.edu.unmsm.fisi.gestiondocente.teacher.dto.TeacherProfile;
import pe.edu.unmsm.fisi.gestiondocente.teacher.service.TeacherService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<DefaultResponse<TeacherProfile>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        TeacherProfile profile = teacherService.getTeacherProfileByPersonId(principal.personId());
        return ResponseEntity.ok(DefaultResponse.success("Información del perfil docente", profile));
    }
}
