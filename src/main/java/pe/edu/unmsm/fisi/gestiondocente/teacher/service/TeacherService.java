package pe.edu.unmsm.fisi.gestiondocente.teacher.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unmsm.fisi.gestiondocente.teacher.dto.TeacherProfile;
import pe.edu.unmsm.fisi.gestiondocente.teacher.entitiy.Teacher;
import pe.edu.unmsm.fisi.gestiondocente.teacher.repository.TeacherRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public TeacherProfile getTeacherProfileByPersonId(Long personId) {
        if (personId == null) {
            throw new IllegalArgumentException("El identificador de persona no puede ser nulo");
        }

        Teacher teacher = teacherRepository.findByPersonId(personId)
                .orElseThrow(() -> new NoSuchElementException("Perfil de docente no encontrado para persona: ID"));

        return new TeacherProfile(
                teacher.getId(),
                teacher.getPerson().getId(),
                teacher.getMoodleId(),
                teacher.getCode(),
                teacher.getPerson().getDni(),
                teacher.getPerson().getFirstName(),
                teacher.getPerson().getPaternalLastName(),
                teacher.getPerson().getMaternalLastName(),
                teacher.getDepartment(),
                teacher.getPerson().getRegisterState()
        );
    }
}
