\c av_certifications_db;

BEGIN;

-- 1. Catálogos Base
INSERT INTO "system_role" ("code", "name", "description", "active")
VALUES
('ADMIN', 'Administrador', 'Acceso total al sistema de certificación', true),
('DOCENTE', 'Docente', 'Docente evaluado y titular de aula', true);

INSERT INTO "academic_period" ("semester_code", "name", "description", "start_date", "end_date")
VALUES
('26.1', 'Semestre Académico 2026-1', 'Primer periodo regular 2026', '2026-03-15', '2026-07-20'),
('26.2', 'Semestre Académico 2026-2', 'Segundo periodo regular 2026', '2026-08-15', '2026-12-20');

INSERT INTO "course" ("code", "name", "description")
VALUES
('202W0701', 'Ingeniería de Software I', 'Fundamentos de desarrollo y ciclos de vida'),
('202W0702', 'Bases de Datos I', 'Diseño y administración de BD'),
('202W0703', 'Arquitectura de Software', 'Patrones y diseño de sistemas distribuidos');


-- 2. Personas (Admin, Docente)
INSERT INTO "person" ("first_name", "paternal_last_name", "maternal_last_name", "dni")
VALUES
('ADMIN', 'USER', 'TEST', '00000000'),
('LAZARO FLORIAN', 'MOTA', 'ALVA', '11122233'),
('LUIS ALBERTO', 'ALARCON', 'LOAYZA', '22233344'),
('CARLOS EDMUNDO', 'NAVARRO', 'DEPAZ', '33344455');

-- 3. Cuentas Institucionales (LDAP / UNMSM)
INSERT INTO "institutional_account"
("person_id", "ldap_uid", "institutional_email", "main", "account_status")
VALUES
(1, 'aulavirtual.fisi', 'aulavirtual.fisi@unmsm.edu.pe', true, 'ACTIVO'),
(2, 'lmotaa', 'lmotaa@unmsm.edu.pe', true, 'ACTIVO'),
(3, 'lalarconl', 'lalarconl@unmsm.edu.pe', true, 'ACTIVO'),
(4, 'cnavarrod', 'cnavarrod@unmsm.edu.pe', true, 'ACTIVO');

-- 4. Asignación de Roles de Sistema
INSERT INTO "account_system_role"
("account_id", "system_role_id", "granted_by_account_id", "active")
VALUES
(1, 1, 1, true),
(2, 2, 1, true),
(3, 2, 1, true),
(4, 2, 1, true);


-- 5. Registro de Docente
INSERT INTO "teacher"
("person_id", "code", "moodle_id", "department")
VALUES
(2, 22200100, 45, 'CC'),
(3, 22200101, 46, 'CC'),
(4, 22200102, 47, 'SW');


-- 6. Carga Académica
INSERT INTO "academic_workload"
("course_id", "academic_period_id", "teacher_id", "moodle_id", "cycle", "section", "school", "plan")
VALUES
(1,1,2,100,8,1,'SW',2018),
(1,1,3,101,8,2,'SW',2018),
(2,1,2,102,9,1,'SW',2018),
(1,1,3,103,9,2,'SW',2018),
(3,1,2,104,10,1,'SW',2018);


-- 9. Certificaciones
INSERT INTO "certification"
("academic_workload_id", "document_path", "status")
VALUES
(1, '/storage/certs/2026-1/CERT-202W0701-S1.pdf', 'EMITIDO'),
(3, '/storage/certs/2026-1/CERT-202W0702-S1.pdf', 'VERIFICADO');

COMMIT;