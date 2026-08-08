package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class SemesterCertificateRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void debeConservarContratoJsonConSnakeCase() throws Exception {
        String json = """
                {
                  "teacher_code": "22200275",
                  "semester": "26.1",
                  "expected_courses": [
                    {
                      "code": "32BGNYGF",
                      "section": "1"
                    }
                  ]
                }
                """;

        SemesterCertificateRequest request = objectMapper.readValue(json, SemesterCertificateRequest.class);

        assertThat(request.getTeacherCode()).isEqualTo("22200275");
        assertThat(request.getSemester()).isEqualTo("26.1");
        assertThat(request.getExpectedCourses()).hasSize(1);
        assertThat(request.getExpectedCourses().get(0).getCode()).isEqualTo("32BGNYGF");
        assertThat(request.getExpectedCourses().get(0).getSection()).isEqualTo("1");
    }
}
