# API standards

The public backend API uses `/api/v1` as the versioned prefix.

## Official public endpoints

- `POST /api/v1/auth/login`
- `GET /api/v1/teachers/me`
- `GET /api/v1/teachers/me/courses`
- `GET /api/v1/teachers`
- `GET /api/v1/certificates`
- `POST /api/v1/certificates`
- `GET /api/v1/certificates/{id}`
- `GET /api/v1/certificates/{id}/document`
- `GET /api/v1/certificates/{id}/versions`
- `POST /api/v1/certificates/{id}/signature`

New public JSON endpoints must use English resource names, plural collections, lower camel case properties, `Id` suffix for identifiers, and `At` suffix for timestamps. Query filters must be sent as query parameters. Paginated endpoints use `page` and `size`; teacher courses default to `page=0`, `size=10`, and reject `size > 50`.

Certificate list endpoints accept filters such as `teacherCode`, `certificateType`, `status`, `semester`, and `course`. Certificate document delivery uses `GET /api/v1/certificates/{id}/document?disposition=inline|attachment`; new APIs should not introduce verb-style paths such as `/download`.

`GET /api/v1/teachers` is the official collection endpoint for director/admin views. `DIRECTOR` users are restricted to their own academic department, while `ADMIN` can list all teachers or filter by `department`.

## Success envelope

JSON endpoints return:

```json
{
  "success": true,
  "message": "Operación completada exitosamente",
  "data": {}
}
```

Paginated endpoints add:

```json
{
  "pagination": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 0,
    "totalPages": 0,
    "numberOfElements": 0
  }
}
```

## Error envelope

JSON errors use:

```json
{
  "success": false,
  "statusCode": 400,
  "error": "Bad Request",
  "message": "Los datos enviados contienen errores de validación",
  "path": "/api/v1/resource",
  "timestamp": "2026-08-19T15:52:29.560Z",
  "details": []
}
```

## Temporary legacy endpoints

These routes remain only for compatibility while the frontend and API contract are migrated:

- `/api/v1/auth/me`
- `/api/v1/docentes/**`
- `/api/v1/constancias/**`
- `/api/v1/director/**`

Do not add new public routes under these Spanish segments. The current frontend MVP uses the official `/api/v1/teachers/**` and `/api/v1/certificates/**` routes; `/api/v1/constancias/**` and `/api/v1/director/**` remain available only as temporary compatibility layers for external or historical consumers.
