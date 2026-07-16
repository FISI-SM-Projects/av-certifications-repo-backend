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

- `auth/`: autenticacion demo y sesion simulada.
- `common/`: utilidades y estructura comun.
- `config/`: configuracion Spring y CORS.
- `constancia/`: generacion, consulta, PDF, validacion y persistencia de constancias.
- `docente/`: perfiles y datos demo de docentes.
- `periodo/`: soporte demo de periodos academicos.
- `usuario/`: usuarios demo y roles.

`src/main/resources/`

- `application.properties`: configuracion base.
- `fonts/`: fuentes usadas para generar PDFs.

`src/test/`

- Pruebas unitarias e integrales del backend.
