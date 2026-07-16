# Gestion Docente FISI - Backend

Backend del Sistema de Gestion Docente y Constancias de la FISI. Esta desarrollado con Spring Boot, expone una API REST para el frontend Next.js y mantiene persistencia local de constancias versionadas.

El proyecto usa datos demo y autenticacion simulada. No incluye Moodle real, base de datos, JPA, aprobacion formal, QR ni firma digital.

## Requisitos

- Java 21 o superior compatible con el `pom.xml`.
- Acceso a red solo si Maven Wrapper necesita descargar Maven o dependencias por primera vez.
- No es necesario instalar Maven globalmente.

## Ejecucion local

Windows:

```powershell
.\mvnw.cmd --version
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:start
.\mvnw.cmd spring-boot:stop
```

Linux/macOS:

```bash
./mvnw --version
./mvnw clean test
./mvnw spring-boot:start
./mvnw spring-boot:stop
```

`spring-boot:run` tambien funciona, pero queda en primer plano.

## Configuracion por entorno

Variables soportadas:

```env
APP_STORAGE_ROOT=storage
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```

`APP_STORAGE_ROOT` define la raiz local de almacenamiento. El valor por defecto es `storage`.

`APP_CORS_ALLOWED_ORIGINS` acepta una lista separada por comas. Por defecto permite:

```text
http://localhost:3000
http://localhost:3001
```

No versionar archivos `.env` reales. Usar `.env.example` como referencia segura.

## Endpoints principales

| Metodo | Ruta | Proposito |
| ------ | ---- | --------- |
| GET | `/api/v1/health` | Verificar que el backend responde. |
| GET | `/api/v1/auth/demo-users` | Listar usuarios demo. |
| POST | `/api/v1/auth/demo-login` | Login simulado por correo. |
| GET | `/api/v1/docentes/demo/perfil` | Perfil demo consolidado. |
| GET | `/api/v1/docentes/{teacherCode}/perfil` | Perfil docente consolidado por codigo. |
| GET | `/api/v1/director/docentes?departamentoAcademico={departamento}` | Docentes por departamento. |
| POST | `/api/v1/constancias/curso` | Generar constancia por curso. |
| POST | `/api/v1/constancias/semestral` | Generar constancia semestral. |
| GET | `/api/v1/constancias/docentes/{teacherCode}` | Ultimas constancias visibles del docente. |
| GET | `/api/v1/constancias/generaciones/{generationId}` | Metadata publica de una generacion. |
| GET | `/api/v1/constancias/certificados/{certificateKey}/historial` | Historial de versiones. |
| GET | `/api/v1/constancias/generaciones/{generationId}/pdf` | Visualizar PDF. |
| GET | `/api/v1/constancias/generaciones/{generationId}/download` | Descargar PDF. |

## Persistencia local

La persistencia usa filesystem local, no base de datos:

```text
storage/certificates/course/{semester}/{teacherCode}/{courseCode}-{section}/vNNN/
storage/certificates/semester/{semester}/{teacherCode}/vNNN/
```

Archivos por generacion:

- `request.json` para constancias por curso.
- `source-summary.json` para constancias semestrales.
- `metadata.json`.
- `certificate.pdf`.

`storage/**` esta ignorado salvo `storage/.gitkeep`. No compartir `storage/` real con datos locales.

## Fechas

Las fechas auditables de constancias se serializan con zona explicita como `Instant`, por ejemplo:

```text
2026-07-16T19:30:00Z
```

La lectura mantiene compatibilidad con metadata historica sin zona, interpretandola como `America/Lima`.

## Pruebas

```powershell
.\mvnw.cmd clean test
```

La cantidad actual de pruebas se informa al ejecutar la suite.

Las pruebas usan almacenamiento temporal y no deben escribir en `storage/` real.

## Exportacion limpia

Generar un ZIP compartible sin artefactos locales:

```powershell
.\scripts\export-clean.ps1
```

En Linux/macOS, si `zip` esta disponible:

```bash
./scripts/export-clean.sh
```

El ZIP excluye `.git/`, `target/`, `storage/certificates/`, variables `.env`, logs, PDFs y JSON generados. No versionar los ZIP generados.

## Limitaciones

- Autenticacion simulada.
- Sin Moodle real.
- Sin aprobacion formal por director.
- Sin base de datos ni JPA.
- Sin Spring Security, JWT ni LDAP.
- Sin QR ni firma digital.

Trabajar sobre la rama local `progress` para los bloques de saneamiento previos al Sprint 4.
