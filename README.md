# Gestion Docente FISI - Backend

API REST en Spring Boot para la gestion docente y la generacion de constancias.

## Requisitos

- Java 21.
- Maven Wrapper incluido en el repositorio.

## Levantar el backend

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

El backend queda disponible por defecto en:

```text
http://localhost:8080
```

## Estructura

`src/main/java/pe/edu/unmsm/fisi/gestiondocente/`

La estructura es layer-first: primero se ven las capas tecnicas y dentro de cada capa se agrupan los modulos funcionales como `auth`, `usuario`, `docente`, `constancia` y `periodo`.

- `controller/`: endpoints HTTP y handlers por modulo.
- `service/`: reglas de negocio, orquestacion y generacion PDF.
- `repository/`: contratos y persistencia demo/filesystem.
- `entity/`: entidades de dominio.
- `dto/`: contratos de entrada y salida.
- `mapper/`: conversion entre entidades y DTO.
- `exception/`: excepciones de dominio.
- `validation/`: normalizacion y validacion.
- `serialization/`: compatibilidad de serializacion.
- `config/`: configuracion Spring y CORS.
- `common/`: utilidades o respuestas comunes si aplican.

`Usuario` representa cuenta, sesion y rol. `Docente` representa el perfil academico/profesional y es la fuente de datos como nombre, correo institucional y departamento.

`src/main/resources/`

- `application.properties`: configuracion base.
- `fonts/`: fuentes usadas para generar PDFs.

`src/test/`

- Pruebas unitarias e integrales del backend.
