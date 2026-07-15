# Gestion Docente FISI - Backend

Backend del Sistema de Gestion Docente y Constancias de la FISI. Esta desarrollado con Spring Boot y expone una API REST para el frontend Next.js.

El proyecto queda al cierre del Sprint 2 con datos simulados en memoria, autenticacion demo por rol y consultas base para las vistas de docente, director y administrador.

## Tecnologias

- Java 21
- Spring Boot
- Maven Wrapper
- JUnit / MockMvc

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

## Usuarios y roles demo

Roles disponibles:

- `DOCENTE`
- `DIRECTOR`
- `ADMIN`

La autenticacion es simulada:

- no hay contrasena;
- no hay token;
- no hay sesion HTTP real;
- los datos se almacenan en memoria;
- no representa seguridad de produccion.

## Estado funcional

- Login demo por correo.
- Usuarios demo para docente, director y administrador.
- Consulta de docentes por Departamento Academico.
- Consulta de perfil docente por codigo.
- Perfil docente demo del Sprint 1 conservado.
- Constancias demo con estados `GENERADO` y `APROBADO`.

## Limitaciones

- Sin base de datos.
- Sin JPA.
- Sin Spring Security.
- Sin JWT.
- Sin LDAP.
- Sin aprobacion real de constancias.
- Sin generacion PDF.
- Sin persistencia.

## Pruebas

Resultado actual de cierre del Sprint 2:

- 36 tests ejecutados.
- 0 fallos.
- 0 errores.
- 0 omitidos.

Si Maven Wrapper necesita descargar dependencias o la distribucion de Maven, puede requerir acceso de red en entornos restringidos.

## Siguiente sprint

El siguiente sprint deberia enfocarse en el flujo real de constancias: aprobacion, generacion de PDF, trazabilidad y reemplazo progresivo de la autenticacion simulada por seguridad real cuando corresponda.
