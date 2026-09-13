# API standards

The public backend API follows the Aula Virtual/Apidog contract directly. It does not expose a public `/api/v1` prefix.

## Official public endpoints

- `POST /auth/login`
- `GET /teachers/me`
- `GET /teachers/me/courses`
- `GET /teachers`
- `GET /certificates`
- `POST /certificates`
- `GET /certificates/{id}`
- `GET /certificates/{id}/document`
- `GET /certificates/{id}/versions`
- `POST /certificates/{id}/signature`

New public JSON endpoints must use English resource names, plural collections, lower camel case properties, `Id` suffix for identifiers, and `At` suffix for timestamps. Query filters must be sent as query parameters. Paginated endpoints use `page` and `size`; teacher courses default to `page=0`, `size=10`, and reject `size > 50`.

Certificate list endpoints accept filters such as `teacherCode`, `certificateType`, `status`, `semester`, and `course`. Certificate document delivery uses `GET /certificates/{id}/document?disposition=inline|attachment`; new APIs should not introduce verb-style paths such as `/download`.

`GET /teachers` is the official collection endpoint for director/admin views. `DIRECTOR` users are restricted to their own academic department, while `ADMIN` can list all teachers or filter by `department`.

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
  "path": "/resource",
  "timestamp": "2026-08-19T15:52:29.560Z",
  "details": []
}
```

## Removed legacy endpoints

The final public contract does not expose versioned, Spanish, demo, temporary, or legacy routes. Do not add public endpoints under:

- `/api/v1/**`
- `/docentes/**`
- `/constancias/**`
- `/director/**`
- `/demo/**`
- `/legacy/**`
- `/temp/**`

The current frontend MVP consumes only `/auth/login`, `/teachers/**`, `/certificates/**`, and `/health`.

## Visible signature

`POST /certificates/{id}/signature` applies a non-cryptographic institutional visible signature to the PDF. The signed document is served by `GET /certificates/{id}/document` when the certificate status is `FIRMADA`.
