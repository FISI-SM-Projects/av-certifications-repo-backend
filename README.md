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

## Alcance actual

No usa base de datos real, JPA, login, LDAP, seguridad avanzada, PDF real ni firma digital.
