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

## Arquitectura

`src/main/java/pe/edu/unmsm/fisi/gestiondocente/`

Monolito modular por dominio con capas internas. Primero se ven los modulos funcionales y dentro de cada modulo se organizan sus capas tecnicas.

- `web/`: controladores, DTOs HTTP y exception handlers.
- `application/`: casos de uso, servicios, validadores, mappers y puertos.
- `domain/`: entidades, estados, reglas y excepciones de dominio.
- `infrastructure/`: repositorios, filesystem, PDF, serializacion y adaptadores tecnicos.
- `shared/`: configuracion y utilidades transversales.

Modulos actuales:

- `shared`
- `auth`
- `usuario`
- `docente`
- `periodo`
- `constancia`

Modulos previstos:

- `curso`
- `cargadocente`
- `integracionaulavirtual`
- `revision`
- `auditoria`

`Usuario` representa cuenta, sesion y rol. `Docente` representa el perfil academico/profesional y es la fuente de datos como nombre, correo institucional y departamento.

`src/main/resources/`

- `application.properties`: configuracion base.
- `fonts/`: fuentes usadas para generar PDFs.

`src/test/`

- Pruebas unitarias e integrales del backend.
