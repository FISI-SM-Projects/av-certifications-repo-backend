package pe.edu.unmsm.fisi.gestiondocente.academic.dto;

import java.time.LocalDate;

public record AcademicPeriodDTO(
   Long id,
   String semesterCode,
   LocalDate startDate,
   LocalDate endDate
) {
}
