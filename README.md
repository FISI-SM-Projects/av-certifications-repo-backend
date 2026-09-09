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

Para uso real local se requiere PostgreSQL institucional y arranque seguro con
`SPRING_JPA_HIBERNATE_DDL_AUTO=validate` y `SPRING_SQL_INIT_MODE=never`.
La autenticacion usa Spring Security, LDAP y JWT; no existe modo demo funcional
en la interfaz de producto.

## Arquitectura

`src/main/java/pe/edu/unmsm/fisi/gestiondocente/`

Monolito modular por dominio con capas clasicas internas. Se eligio esta estructura para que nuevos mantenedores ubiquen rapido controladores, servicios, repositorios, entidades y DTOs dentro de cada modulo funcional.

- `controller/`: endpoints REST y exception handlers.
- `service/`: logica de aplicacion y servicios de negocio.
- `repository/`: acceso a datos, filesystem o persistencia.
- `entity/`: entidades, enums y objetos del dominio persistente/simple.
- `dto/`: contratos de entrada y salida.
- `mapper/`: conversion entre entidades y DTOs.
- `validation/`: validaciones de entrada y reglas de solicitud.
- `serialization/`: serializacion tecnica especifica del modulo.
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

`database_postgresql/`

- `av-database.sql`: esquema base aprobado.
- `av_minimal_dev_data.sql`: datos minimos de desarrollo.
- `migrations/`: cambios no destructivos versionados, incluida la distincion
  `COURSE`/`SEMESTER` para certificaciones.

`src/test/`

- Pruebas unitarias e integrales del backend.
