# Datos DEV limpios

`av_minimal_dev_data_clean.sql` reconstruye un conjunto minimo de datos institucionales para demostraciones locales.

Uso recomendado:

```powershell
$env:PGPASSWORD='postgres'
pg_dump -h localhost -p 5433 -U postgres -d av_certifications_db -f "$env:TEMP\av_certifications_db_before_clean_seed.sql"
psql -h localhost -p 5433 -U postgres -d av_certifications_db -f database_postgresql/dev/av_minimal_dev_data_clean.sql
```

El script es solo para DEV/TEST porque usa `TRUNCATE ... RESTART IDENTITY CASCADE` sobre datos de prueba. No debe ejecutarse en produccion.

Cuenta principal de demostracion: `lalarconl`, docente `22200101`, con 6 cargas academicas en `26.1` y 0 constancias iniciales. El flujo esperado es generar las 6 constancias COURSE desde la UI y luego generar la constancia SEMESTER.

Las credenciales LDAP de desarrollo se mantienen en `src/main/resources/users.ldif`.
