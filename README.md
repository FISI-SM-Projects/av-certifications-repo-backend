# Gestion Docente FISI - Backend

Backend del Sistema de Gestion Docente y Constancias de la FISI. Esta desarrollado con Spring Boot y expone una API REST para el frontend Next.js.

Actualmente publica el Perfil Docente con datos simulados en memoria. No usa base de datos ni seguridad real.

## Requisitos

- Java 21

No es necesario instalar Maven: el proyecto usa Maven Wrapper.

## Ejecucion local

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Puerto local:

```text
8080
```

## Endpoints actuales

| Metodo | Ruta                         | Proposito                              |
| ------ | ---------------------------- | -------------------------------------- |
| GET    | `/api/v1/health`             | Verificar que el backend responde.     |
| GET    | `/api/v1/docentes/demo/perfil` | Obtener el Perfil Docente demo.       |

## Estructura

El codigo principal vive en:

```text
src/main/java/pe/edu/unmsm/fisi/gestiondocente/
```

La anidacion corresponde al package Java:

```java
pe.edu.unmsm.fisi.gestiondocente
```

Estructura resumida:

```text
gestiondocente/
|-- common/
|-- config/
|-- docente/
|-- constancia/
`-- periodo/
```

- `common/`: elementos compartidos como health check, respuestas, excepciones y utilidades.
- `config/`: configuracion transversal, incluida la configuracion web y CORS.
- `docente/`: datos y operaciones del perfil docente.
- `constancia/`: datos y operaciones relacionadas con constancias.
- `periodo/`: datos y operaciones relacionadas con periodos academicos.

Capas internas usadas por los modulos:

```text
controller -> service -> repository
entity -> mapper -> dto
```

Para agregar nuevas funcionalidades, crear o extender un modulo funcional manteniendo esa separacion por capas.

## Integracion con frontend

El frontend esperado se ejecuta en:

```text
http://localhost:3000
```

El CORS de desarrollo permite solicitudes desde `http://localhost:3000` y `http://localhost:3001`.

El frontend consume esta API mediante la variable:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## Estado y limitaciones actuales

- Datos simulados en memoria.
- Sin base de datos.
- Sin JPA.
- Sin LDAP.
- Sin seguridad real.
- Sin generacion PDF.
- Sin firma digital.

## Sprint 2: Autenticación y roles simulados

El Sprint 2 agregara una autenticacion simulada para trabajar con usuarios demo, identificar su rol y, cuando corresponda, su Departamento Academico. Servira como preparacion para rutas diferenciadas por rol, pero no sera seguridad real: no usara LDAP, JWT, Spring Security ni base de datos.

Reglas funcionales previstas:

- `DOCENTE`: accede a su perfil docente, sus constancias y funciones relacionadas con sus propias constancias.
- `DIRECTOR`: pertenece a un unico Departamento Academico y accede al dashboard de direccion, perfiles de docentes de su departamento, constancias generadas por docentes de su departamento y un flujo de aprobacion futuro.
- `ADMIN`: accede a todas las areas, todos los docentes, todas las constancias y funciones administrativas futuras; en este sprint tendra una interfaz minima.

Departamentos iniciales:

```text
Ingeniería de Software
Ciencia de la Computación
```

Usuarios demo esperados:

- docente de Ingenieria de Software;
- docente de Ciencia de la Computacion;
- director de Ingenieria de Software;
- director de Ciencia de la Computacion;
- administrador.

Endpoints futuros del Sprint 2, usando datos simulados en memoria:

| Metodo | Ruta                                                               | Proposito                                      |
| ------ | ------------------------------------------------------------------ | ---------------------------------------------- |
| GET    | `/api/v1/auth/demo-users`                                          | Listar usuarios demo disponibles.              |
| POST   | `/api/v1/auth/demo-login`                                          | Crear una sesion simulada para un usuario demo. |
| GET    | `/api/v1/director/docentes?departamentoAcademico={departamento}`   | Consultar docentes por Departamento Academico. |
| GET    | `/api/v1/docentes/{teacherCode}/perfil`                            | Consultar el perfil de un docente por codigo.  |

Contrato funcional de sesion esperado:

```json
{
  "user": {
    "id": 1,
    "fullName": "Director Software",
    "email": "director.software@unmsm.edu.pe",
    "role": "DIRECTOR",
    "departamentoAcademico": "Ingeniería de Software",
    "teacherCode": null
  }
}
```

`role` define el tipo de usuario. `departamentoAcademico` limita el alcance del director. `teacherCode` vincula un usuario docente con su perfil. Para `ADMIN`, `departamentoAcademico` y `teacherCode` pueden ser `null`.

La restriccion sera simulada: un docente solo ve lo suyo, un director solo ve docentes y constancias de su Departamento Academico, y admin tiene acceso general. La autorizacion real se implementara en un sprint futuro.
