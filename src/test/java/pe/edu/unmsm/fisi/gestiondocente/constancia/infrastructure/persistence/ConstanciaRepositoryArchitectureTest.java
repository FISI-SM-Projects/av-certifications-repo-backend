package pe.edu.unmsm.fisi.gestiondocente.constancia.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Primary;

import pe.edu.unmsm.fisi.gestiondocente.constancia.application.ConstanciaQueryService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.application.CourseCertificateService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.application.SemesterCertificateService;
import pe.edu.unmsm.fisi.gestiondocente.constancia.application.port.CertificateGenerationRepository;

class ConstanciaRepositoryArchitectureTest {

    @Test
    void filesystemDebeImplementarRepositorioDeGeneraciones() {
        assertThat(CertificateGenerationRepository.class).isAssignableFrom(FileSystemConstanciaRepository.class);
    }

    @Test
    void demoNoDebeImplementarRepositorioDeGeneracionesNiSerPrimary() {
        assertThat(CertificateGenerationRepository.class.isAssignableFrom(DemoConstanciaRepository.class)).isFalse();
        assertThat(DemoConstanciaRepository.class.isAnnotationPresent(Primary.class)).isFalse();
    }

    @Test
    void repositorioRealNoDebeTenerMetodosDefaultUnsupported() {
        assertThat(Arrays.stream(CertificateGenerationRepository.class.getMethods())
                .filter(Method::isDefault)
                .toList()).isEmpty();
    }

    @Test
    void serviciosRealesDebenDependerDelRepositorioDeGeneraciones() {
        assertThat(repositoryFieldTypes(CourseCertificateService.class))
                .contains(CertificateGenerationRepository.class)
                .doesNotContain(DemoConstanciaRepository.class, LegacyConstanciaRepository.class);
        assertThat(repositoryFieldTypes(SemesterCertificateService.class))
                .contains(CertificateGenerationRepository.class)
                .doesNotContain(DemoConstanciaRepository.class, LegacyConstanciaRepository.class);
        assertThat(repositoryFieldTypes(ConstanciaQueryService.class))
                .contains(CertificateGenerationRepository.class)
                .doesNotContain(DemoConstanciaRepository.class, LegacyConstanciaRepository.class);
    }

    private static Class<?>[] repositoryFieldTypes(Class<?> serviceType) {
        return Arrays.stream(serviceType.getDeclaredFields())
                .map(Field::getType)
                .filter(type -> type.getSimpleName().contains("Repository"))
                .toArray(Class<?>[]::new);
    }
}
