BEGIN;

-- 1. Catálogos Base
INSERT INTO "document_type"
("code", "name", "description", "min_length", "max_length", "regex", "active")
VALUES
('DNI', 'Documento Nacional de Identidad', 'DNI Perú (8 dígitos)', 8, 8, '^[0-9]{8}$', true);

INSERT INTO "contact_type"
("code", "name", "description", "visibility_level", "active")
VALUES
('EMAIL_PERS', 'Correo Personal', 'Correo electrónico no institucional', 'INTERNO', true),
('PHONE_MOB', 'Celular', 'Número de telefonía móvil', 'INTERNO', true);

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
INSERT INTO "person" ("first_name", "paternal_last_name", "maternal_last_name")
VALUES
('ADMIN', 'USER', 'TEST'),
('LAZARO FLORIAN', 'MOTA', 'ALVA'),
('LUIS ALBERTO', 'ALARCON', 'LOAYZA'),
('CARLOS EDMUNDO', 'NAVARRO', 'DEPAZ');


-- 3. Documentos de Identidad (DNI Perú)
INSERT INTO "person_document"
("document_type_id", "person_id", "value", "origin_country", "main", "effective_date", "expiration_date")
VALUES
(1, 2, '11122233','PE', true, '2020-01-01', '2030-01-01'),
(1, 3, '22233344','PE', true, '2020-01-01', '2030-01-01'),
(1, 4, '33344455','PE', true, '2020-01-01', '2030-01-01');


-- 4. Cuentas Institucionales (LDAP / UNMSM)
INSERT INTO "institutional_account"
("person_id", "ldap_uid", "institutional_email", "main", "account_status")
VALUES
(2, 'lmotaa', 'lmotaa@unmsm.edu.pe', true, 'ACTIVO'),
(3, 'lalarconl', 'lalarconl@unmsm.edu.pe', true, 'ACTIVO'),
(4, 'cnavarrod', 'cnavarrod@unmsm.edu.pe', true, 'ACTIVO'),
(1, 'aulavirtual.fisi', 'aulavirtual.fisi@unmsm.edu.pe', true, 'ACTIVO');


-- 5. Contactos Personales (Email Personal y Celular)
INSERT INTO "person_contact"
("person_id", "contact_type_id", "value", "main", "effective_date", "expiration_date")
VALUES
(2, 1, 'lmotaa.personal@gmail.com', false, '2026-01-01', '2030-12-31'),
(2, 2, '+51987654321', true, '2026-01-01', '2030-12-31'),
(3, 2, '+51999888777', true, '2026-01-01', '2030-12-31');


-- 6. Asignación de Roles de Sistema
INSERT INTO "person_system_role"
("person_id", "system_role_id", "granted_by_person_id", "active")
VALUES
(1, 1, 1, true),
(2, 2, 1, true),
(3, 2, 1, true),
(4, 2, 1, true);


-- 7. Registro de Docente
INSERT INTO "teacher"
("person_id", "code", "moodle_id", "department")
VALUES
(2, 22200100, 45, 'CC'),
(3, 22200101, 46, 'CC'),
(4, 22200102, 47, 'SW');


-- 8. Carga Académica
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