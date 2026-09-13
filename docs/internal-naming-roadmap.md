# Internal naming roadmap

The public API contract is already in English and must stay that way. This document tracks the gradual cleanup of internal Spanish technical names without a risky broad rename.

## Cleaned in this checkpoint

- Removed obsolete security matchers for removed public routes.
- Added English-named PDF layout helpers around institutional headers, footers, and bordered tables.
- Kept public DTOs for `/teachers/**` and `/certificates/**` in English lower camel case.

## Remaining internal Spanish names

- Legacy packages and classes under `docente`, `constancia`, `usuario`, `periodo`, and related test fixtures.
- Compatibility DTOs and services such as `DocenteProfileQueryService`, `ConstanciaQueryService`, `EstadoConstancia`, and filesystem legacy repository helpers.
- Spanish exception messages and PDF body text, which are acceptable user-facing Spanish content.

## Suggested cleanup order

1. Frontend service and type names for certificate view models.
2. Backend PDF helper internals that are not part of persisted or public contracts.
3. Backend certificate package internals, keeping public `/certificates/**` DTOs stable.
4. Teacher and workload internals.
5. Database naming only if a future institutional migration explicitly approves it.

## Guardrails

- Do not rename public paths, JSON fields, database columns, or migration history in a naming-only task.
- Keep changes module-scoped and covered by tests.
- Preserve visible Spanish UI copy for FISI users.
