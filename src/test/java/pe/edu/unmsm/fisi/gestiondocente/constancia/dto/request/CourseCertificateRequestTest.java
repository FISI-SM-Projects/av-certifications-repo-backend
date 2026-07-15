package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class CourseCertificateRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void debeConservarContratoJsonConSnakeCase() throws Exception {
        String json = """
                {
                  "teacher": {
                    "full_name": "Nombre completo docente",
                    "email": "correodocente@unmsm.edu.pe",
                    "teacher_code": "22200275"
                  },
                  "course": {
                    "code": "32BGNYGF",
                    "subject": "Nombre del curso",
                    "cycle": "7",
                    "section": "1",
                    "school": "SW",
                    "plan": "2023",
                    "semester": "26.1"
                  },
                  "issuer": {
                    "system": "moodle",
                    "executed_by_userid": "12345",
                    "executed_by_email": "usuario@unmsm.edu.pe"
                  }
                }
                """;

        CourseCertificateRequest request = objectMapper.readValue(json, CourseCertificateRequest.class);

        assertThat(request.getTeacher().getFullName()).isEqualTo("Nombre completo docente");
        assertThat(request.getTeacher().getTeacherCode()).isEqualTo("22200275");
        assertThat(request.getCourse().getCycle()).isEqualTo("7");
        assertThat(request.getCourse().getSection()).isEqualTo("1");
        assertThat(request.getCourse().getPlan()).isEqualTo("2023");
        assertThat(request.getCourse().getSemester()).isEqualTo("26.1");
        assertThat(request.getIssuer().getExecutedByUserid()).isEqualTo("12345");
        assertThat(request.getIssuer().getExecutedByEmail()).isEqualTo("usuario@unmsm.edu.pe");
    }
}
