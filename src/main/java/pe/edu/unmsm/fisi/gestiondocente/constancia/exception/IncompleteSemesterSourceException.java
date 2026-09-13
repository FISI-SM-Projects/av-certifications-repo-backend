package pe.edu.unmsm.fisi.gestiondocente.constancia.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import pe.edu.unmsm.fisi.gestiondocente.shared.exception.BaseDomainException;
import pe.edu.unmsm.fisi.gestiondocente.shared.response.ErrorDetails;

public class IncompleteSemesterSourceException extends BaseDomainException {
    public IncompleteSemesterSourceException(int totalCourses, int generatedCourseCertificates, List<String> missingCourses) {
        super(
                "Aun no se han generado constancias para todos los cursos del periodo",
                HttpStatus.CONFLICT,
                new ErrorDetails[] {
                        new ErrorDetails("totalCourses", Integer.toString(totalCourses)),
                        new ErrorDetails("generatedCourseCertificates", Integer.toString(generatedCourseCertificates)),
                        new ErrorDetails("missingCourses", String.join(", ", missingCourses))
                }
        );
    }
}
