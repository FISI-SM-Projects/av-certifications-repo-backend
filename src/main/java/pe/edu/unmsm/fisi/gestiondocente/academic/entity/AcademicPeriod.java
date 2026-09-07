package pe.edu.unmsm.fisi.gestiondocente.academic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "academic_period")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "semester_code", length = 10, nullable = false, unique = true)
    private String semesterCode;

    @Column(name = "name", length = 80, nullable = false)
    private String name;

    @Builder.Default
    @Column(name = "description", length = 250)
    private String description = "No disponible";

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
}