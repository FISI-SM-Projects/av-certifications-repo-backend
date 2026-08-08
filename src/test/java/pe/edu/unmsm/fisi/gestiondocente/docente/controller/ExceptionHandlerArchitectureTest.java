package pe.edu.unmsm.fisi.gestiondocente.docente.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.ExceptionHandler;

import pe.edu.unmsm.fisi.gestiondocente.auth.controller.DemoAuthExceptionHandler;

class ExceptionHandlerArchitectureTest {

    @Test
    void illegalStateExceptionNoDebeMapearseComoErrorDeDominio() {
        assertThat(exceptionHandlerTypes(DocenteExceptionHandler.class))
                .doesNotContain(IllegalStateException.class);
        assertThat(exceptionHandlerTypes(DemoAuthExceptionHandler.class))
                .doesNotContain(IllegalStateException.class);
    }

    private static Class<?>[] exceptionHandlerTypes(Class<?> handlerType) {
        return Arrays.stream(handlerType.getDeclaredMethods())
                .map(ExceptionHandlerArchitectureTest::exceptionHandler)
                .filter(annotation -> annotation != null)
                .flatMap(annotation -> Arrays.stream(annotation.value()))
                .toArray(Class<?>[]::new);
    }

    private static ExceptionHandler exceptionHandler(Method method) {
        return method.getAnnotation(ExceptionHandler.class);
    }
}
