# Gestion Docente FISI - Backend

Repositorio backend del Sistema de Gestion Docente FISI.

Contiene una aplicacion Spring Boot con arquitectura por capas para el Sprint 1. Expone el endpoint demo del Perfil Docente con datos simulados en memoria.

## Ejecutar

```cmd
.\mvnw.cmd spring-boot:run
```

Puerto: `8080`.

## CORS de desarrollo

El backend permite solicitudes `GET` hacia `/api/**` desde `http://localhost:3000` y `http://localhost:3001` para la integracion local con Next.js.

## Estructura interna del backend

El codigo Java del backend se encuentra dentro de `src/main/java/`. La ruta `pe/edu/unmsm/fisi/gestiondocente/` representa el package raiz Java:

```java
pe.edu.unmsm.fisi.gestiondocente
```

Esta anidacion es normal en Java y Spring Boot. Los packages usan una convencion de dominio invertido para evitar conflictos de nombres, identificar la organizacion propietaria, mantener consistencia entre archivos y declaraciones `package`, y permitir que Spring Boot descubra componentes dentro del package raiz. No se deben mover clases fuera de esta estructura sin actualizar tambien sus declaraciones `package`.

Estructura resumida:

```text
src/
`-- main/
    |-- java/
    |   `-- pe/
    |       `-- edu/
    |           `-- unmsm/
    |               `-- fisi/
    |                   `-- gestiondocente/
    |                       |-- GestionDocenteApplication.java
    |                       |-- common/
    |                       |   |-- exception/
    |                       |   |-- health/
    |                       |   |-- response/
    |                       |   `-- util/
    |                       |-- config/
    |                       |-- docente/
    |                       |-- constancia/
    |                       `-- periodo/
    `-- resources/
        |-- application.properties
        `-- data.sql
```

`GestionDocenteApplication.java` es el punto de entrada de Spring Boot. Contiene `@SpringBootApplication` e inicia la aplicacion.

`common/` contiene elementos compartidos y transversales, como excepciones, respuestas comunes, utilidades y el health check.

`config/` contiene configuraciones globales, como CORS y configuracion web. En fases posteriores puede alojar configuraciones de seguridad o integracion.

`docente/` es el modulo encargado del perfil y datos del docente.

`constancia/` es el modulo encargado de las constancias.

`periodo/` es el modulo encargado de los periodos academicos.

Cada modulo funcional puede contener estas capas:

```text
controller/
service/
repository/
entity/
dto/
mapper/
```

`controller/` expone endpoints REST, recibe solicitudes HTTP y delega la logica al service. No debe contener logica de negocio compleja.

`service/` contiene la logica de negocio, coordina repositories y mappers, y construye las respuestas necesarias.

`repository/` abstrae el acceso a datos. Actualmente puede contener datos simulados en memoria; en el futuro podra usar JPA o una base de datos sin cambiar el controller.

`entity/` representa objetos internos del dominio. En el futuro puede contener entidades JPA.

`dto/` define los datos expuestos por la API y evita que el frontend dependa directamente de entidades internas.

`mapper/` convierte entidades a DTOs y viceversa para mantener separadas las capas internas y externas.

Flujo por capas:

```text
Cliente HTTP
    |
    v
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Fuente de datos
```

Flujo de respuesta:

```text
Entity
    |
    v
Mapper
    |
    v
DTO
    |
    v
Respuesta JSON
```

Para agregar un nuevo modulo, mantener una estructura similar:

```text
nuevo-modulo/
|-- controller/
|-- service/
|-- repository/
|-- entity/
|-- dto/
`-- mapper/
```

Cada funcionalidad importante debe mantenerse dentro de su modulo. No se deben crear carpetas globales gigantes de controllers o services. Los elementos compartidos van en `common/` y las configuraciones transversales van en `config/`.

## Alcance actual

No usa base de datos real, JPA, login, LDAP, seguridad avanzada, PDF real ni firma digital.
