\c av_certifications_db;

-- DEV/TEST RESET ONLY.
-- Rebuilds a small institutional dataset for local demonstrations.
-- Do not run this script against production data.

BEGIN;

TRUNCATE TABLE
    certification,
    academic_workload,
    teacher,
    account_system_role,
    role_permission,
    institutional_account,
    person,
    course,
    academic_period,
    system_permission,
    system_role
RESTART IDENTITY CASCADE;

INSERT INTO system_role (code, name, description, active)
VALUES
    ('ADMIN', 'Administrador', 'Acceso total al sistema de constancias', true),
    ('DOCENTE', 'Docente', 'Docente titular de carga academica', true),
    ('DIRECTOR', 'Director', 'Director de departamento academico', true);

INSERT INTO academic_period (semester_code, name, description, start_date, end_date)
VALUES
    ('26.1', 'Semestre Academico 2026-1', 'Primer periodo regular 2026', '2026-03-15', '2026-07-20'),
    ('26.2', 'Semestre Academico 2026-2', 'Segundo periodo regular 2026', '2026-08-15', '2026-12-20');

INSERT INTO course (code, name, description)
VALUES
    ('202W0701', 'Ingenieria de Software I', 'Fundamentos de desarrollo y ciclos de vida'),
    ('202W0702', 'Bases de Datos I', 'Diseno y administracion de bases de datos'),
    ('202W0703', 'Arquitectura de Software', 'Patrones y diseno de sistemas distribuidos'),
    ('202W0704', 'Calidad de Software', 'Aseguramiento, metricas y pruebas de software'),
    ('202W0705', 'Gestion de Proyectos de Software', 'Planificacion y seguimiento de proyectos de software'),
    ('202W0706', 'Ingenieria de Requisitos', 'Elicitacion, analisis y gestion de requisitos');

INSERT INTO person (first_name, paternal_last_name, maternal_last_name, dni, register_state)
VALUES
    ('ADMIN', 'USER', 'TEST', '00000000', 'ACTIVO'),
    ('LAZARO FLORIAN', 'MOTA', 'ALVA', '11122233', 'ACTIVO'),
    ('LUIS ALBERTO', 'ALARCON', 'LOAYZA', '22233344', 'ACTIVO'),
    ('CARLOS EDMUNDO', 'NAVARRO', 'DEPAZ', '33344455', 'ACTIVO');

INSERT INTO institutional_account (person_id, ldap_uid, institutional_email, main, account_status)
VALUES
    (1, 'aulavirtual.fisi', 'aulavirtual.fisi@unmsm.edu.pe', true, 'ACTIVO'),
    (2, 'lmotaa', 'lmotaa@unmsm.edu.pe', true, 'ACTIVO'),
    (3, 'lalarconl', 'lalarconl@unmsm.edu.pe', true, 'ACTIVO'),
    (4, 'cnavarrod', 'cnavarrod@unmsm.edu.pe', true, 'ACTIVO');

INSERT INTO account_system_role (account_id, system_role_id, granted_by_account_id, active)
VALUES
    (1, 1, 1, true),
    (2, 3, 1, true),
    (3, 2, 1, true),
    (4, 2, 1, true);

INSERT INTO teacher (person_id, code, moodle_id, department)
VALUES
    (2, '22200100', 45, 'CC'),
    (3, '22200101', 46, 'CC'),
    (4, '22200102', 47, 'SW');

-- Main demonstration account: lalarconl / teacher.code 22200101.
-- Starts with six real 26.1 workloads and zero certifications.
INSERT INTO academic_workload (course_id, academic_period_id, teacher_id, moodle_id, cycle, section, school, plan)
VALUES
    (1, 1, 2, 1001, 8, 1, 'SW', 2018),
    (2, 1, 2, 1002, 9, 1, 'SW', 2018),
    (3, 1, 2, 1003, 10, 1, 'SW', 2018),
    (4, 1, 2, 1004, 8, 1, 'SW', 2018),
    (5, 1, 2, 1005, 9, 1, 'SW', 2018),
    (6, 1, 2, 1006, 10, 1, 'SW', 2018);

-- Secondary accounts remain available for role/session checks.
INSERT INTO academic_workload (course_id, academic_period_id, teacher_id, moodle_id, cycle, section, school, plan)
VALUES
    (1, 1, 1, 2001, 8, 2, 'SW', 2018),
    (2, 1, 3, 2002, 9, 2, 'SW', 2018);

COMMIT;
