# Gestion Docente FISI - Backend

Backend del Sistema de Gestion Docente y Constancias de la FISI. Esta desarrollado con Spring Boot y expone una API REST consumida por el frontend Next.js.

Al cierre del Sprint 3 el proyecto trabaja con datos demo, autenticacion simulada, persistencia local de constancias y generacion de PDF para constancias por curso y semestrales.

## Tecnologias

- Java 21
- Spring Boot
- Maven Wrapper
- JUnit / MockMvc
- PDFBox

## Ejecucion

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:start
.\mvnw.cmd spring-boot:stop
```

`spring-boot:run` tambien funciona, pero queda en primer plano mientras el servidor esta activo.

Puerto local:

```text
8080
```

## Endpoints disponibles

| Metodo | Ruta | Proposito |
| ------ | ---- | --------- |
| GET | `/api/v1/health` | Verificar que el backend responde. |
| GET | `/api/v1/docentes/demo/perfil` | Obtener el perfil docente demo del Sprint 1. |
| GET | `/api/v1/auth/demo-users` | Listar usuarios demo disponibles. |
| POST | `/api/v1/auth/demo-login` | Realizar login simulado por correo. |
| GET | `/api/v1/director/docentes?departamentoAcademico={departamento}` | Consultar docentes por Departamento Academico. |
| GET | `/api/v1/docentes/{teacherCode}/perfil` | Consultar perfil docente por codigo. |
| POST | `/api/v1/constancias/curso` | Generar una constancia por curso. |
| POST | `/api/v1/constancias/semestral` | Generar una constancia semestral a partir de constancias por curso existentes. |
| GET | `/api/v1/constancias/docentes/{teacherCode}` | Listar ultimas versiones visibles de un docente. |
| GET | `/api/v1/constancias/generaciones/{generationId}` | Consultar metadata publica de una generacion. |
| GET | `/api/v1/constancias/certificados/{certificateKey}/historial` | Consultar historial de versiones de una constancia logica. |
| GET | `/api/v1/constancias/generaciones/{generationId}/pdf` | Visualizar PDF en navegador. |
| GET | `/api/v1/constancias/generaciones/{generationId}/download` | Descargar PDF. |

## Usuarios y roles demo

Roles disponibles:

- `DOCENTE`
- `DIRECTOR`
- `ADMIN`

La autenticacion es simulada: no hay contrasena, token, sesion HTTP real, LDAP ni Spring Security.

## Constancias

- Tipo `CURSO`: genera PDF, `request.json`, `metadata.json` y versiona por `teacherCode-courseCode-section-semester`.
- Tipo `SEMESTRAL`: valida cursos esperados, usa la ultima generacion por curso, genera `source-summary.json`, `metadata.json` y PDF.
- Estados permitidos: `GENERADO` y `APROBADO`.
- Si una constancia logica ya tiene una version `APROBADO`, se bloquean nuevas generaciones.
- Los errores principales devuelven JSON controlado: campos faltantes, cursos faltantes, identificadores invalidos, PDF no encontrado y generacion no encontrada.

## Persistencia local

El almacenamiento usa la carpeta local `storage/`:

```text
storage/certificates/course/{semester}/{teacherCode}/{courseCode}-{section}/vNNN/
storage/certificates/semester/{semester}/{teacherCode}/vNNN/
```

Se guardan solicitudes JSON, metadata y PDF. No hay base de datos ni JPA. `storage/**` esta ignorado salvo `storage/.gitkeep`.

## Limitaciones

- Sin Moodle real.
- Sin aprobacion formal por director.
- Sin base de datos.
- Sin JPA.
- Sin Spring Security.
- Sin JWT.
- Sin LDAP.
- Sin QR.
- Sin firma digital.

## Pruebas

Resultado de cierre del Sprint 3:

- 193 tests ejecutados.
- 0 fallos.
- 0 errores.
- 0 omitidos.

Si Maven Wrapper necesita descargar dependencias o la distribucion de Maven, puede requerir acceso de red en entornos restringidos.

## Siguiente sprint

El siguiente sprint deberia enfocarse en aprobacion por director, trazabilidad/auditoria, seguridad real, integracion Moodle y persistencia con base de datos si el alcance lo confirma.
