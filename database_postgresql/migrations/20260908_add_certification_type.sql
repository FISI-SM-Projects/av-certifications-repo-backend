DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE n.nspname = 'public'
          AND t.typname = 'certification_type'
    ) THEN
        CREATE TYPE certification_type AS ENUM ('COURSE', 'SEMESTER');
    END IF;
END $$;

ALTER TABLE certification
    ADD COLUMN IF NOT EXISTS certificate_type certification_type NOT NULL DEFAULT 'COURSE',
    ADD COLUMN IF NOT EXISTS teacher_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS academic_period_id BIGINT NULL;

UPDATE certification c
SET teacher_id = aw.teacher_id,
    academic_period_id = aw.academic_period_id
FROM academic_workload aw
WHERE c.academic_workload_id = aw.id
  AND c.teacher_id IS NULL
  AND c.academic_period_id IS NULL;

ALTER TABLE certification
    ALTER COLUMN academic_workload_id DROP NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'public'
          AND table_name = 'certification'
          AND constraint_name = 'fk_certification_teacher'
    ) THEN
        ALTER TABLE certification
            ADD CONSTRAINT fk_certification_teacher
            FOREIGN KEY (teacher_id) REFERENCES teacher(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'public'
          AND table_name = 'certification'
          AND constraint_name = 'fk_certification_academic_period'
    ) THEN
        ALTER TABLE certification
            ADD CONSTRAINT fk_certification_academic_period
            FOREIGN KEY (academic_period_id) REFERENCES academic_period(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'public'
          AND table_name = 'certification'
          AND constraint_name = 'chk_certification_type_scope'
    ) THEN
        ALTER TABLE certification
            ADD CONSTRAINT chk_certification_type_scope CHECK (
                (
                    certificate_type = 'COURSE'
                    AND academic_workload_id IS NOT NULL
                    AND teacher_id IS NOT NULL
                    AND academic_period_id IS NOT NULL
                )
                OR
                (
                    certificate_type = 'SEMESTER'
                    AND academic_workload_id IS NULL
                    AND teacher_id IS NOT NULL
                    AND academic_period_id IS NOT NULL
                )
            );
    END IF;
END $$;
