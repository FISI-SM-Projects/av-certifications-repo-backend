-- Business rules v2 for institutional certifications.
-- Non-destructive: preserves history and adds metadata required by regeneration/signature rules.

ALTER TYPE certification_status ADD VALUE IF NOT EXISTS 'GENERADA';
ALTER TYPE certification_status ADD VALUE IF NOT EXISTS 'FIRMADA';
ALTER TYPE role ADD VALUE IF NOT EXISTS 'DIRECTOR';

ALTER TABLE certification
  ADD COLUMN IF NOT EXISTS version_number INTEGER,
  ADD COLUMN IF NOT EXISTS academic_snapshot_json JSONB,
  ADD COLUMN IF NOT EXISTS generated_by_account_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS generated_at TIMESTAMP NULL,
  ADD COLUMN IF NOT EXISTS signed_by_account_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS signed_at TIMESTAMP NULL,
  ADD COLUMN IF NOT EXISTS signed_document_path VARCHAR(500) NULL;

UPDATE certification
SET status = 'GENERADA'
WHERE status = 'EMITIDO';

UPDATE certification
SET status = 'FIRMADA'
WHERE status = 'VERIFICADO';

UPDATE certification
SET generated_at = COALESCE(generated_at, created_at)
WHERE generated_at IS NULL;

WITH ranked AS (
  SELECT id,
         ROW_NUMBER() OVER (
           PARTITION BY
             certificate_type,
             CASE WHEN certificate_type = 'COURSE' THEN academic_workload_id END,
             CASE WHEN certificate_type = 'SEMESTER' THEN teacher_id END,
             CASE WHEN certificate_type = 'SEMESTER' THEN academic_period_id END
           ORDER BY id
         ) AS version_value
  FROM certification
)
UPDATE certification c
SET version_number = ranked.version_value
FROM ranked
WHERE ranked.id = c.id
  AND c.version_number IS NULL;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public'
      AND table_name = 'certification'
      AND constraint_name = 'fk_certification_generated_by_account'
  ) THEN
    ALTER TABLE certification
      ADD CONSTRAINT fk_certification_generated_by_account
      FOREIGN KEY (generated_by_account_id) REFERENCES institutional_account(id);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = 'public'
      AND table_name = 'certification'
      AND constraint_name = 'fk_certification_signed_by_account'
  ) THEN
    ALTER TABLE certification
      ADD CONSTRAINT fk_certification_signed_by_account
      FOREIGN KEY (signed_by_account_id) REFERENCES institutional_account(id);
  END IF;
END $$;
