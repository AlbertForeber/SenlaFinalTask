-- db/changelog/changes/003-insert-scopes-and-roles.sql

-- liquibase formatted sql

-- changeset albert:1-insert-scopes
INSERT INTO scopes VALUES
    (1, 'profile:view'),
    (2, 'profile:manage'),
    (3, 'profile:view_admin'),
    (4, 'profile:manage_admin'),
    (5, 'profile:manage_role'),
    (6, 'spot:view'),
    (7, 'spot:view_admin'),
    (8, 'spot:manage'),
    (9, 'scooter:view'),
    (10, 'scooter:view_by_status'),
    (11, 'scooter:manage'),
    (12, 'scooter:view_admin'),
    (13, 'scooter:rent'),
    (14, 'scooter:maintenance'),
    (15, 'trip:view'),
    (16, 'trip:view_period'),
    (17, 'trip:view_admin'),
    (18, 'trip:manage_admin'),
    (19, 'tariff:view'),
    (20, 'tariff:view_admin'),
    (21, 'tariff:subscribe'),
    (22, 'tariff:manage'),
    (23, 'role:view'),
    (24, 'role:manage'),
    (25, 'billing:view_admin'),
    (26, 'billing:manage_admin'),
    (27, 'session:view'),
    (28, 'session:manage');
SELECT setval(pg_get_serial_sequence('scopes', 'id'), COALESCE(MAX(id), 1), true) FROM scopes;
-- rollback DELETE FROM scopes;

-- changeset albert:2-insert-roles
INSERT INTO roles VALUES
    (1, 'user'),
    (2, 'maintenance'),
    (3, 'admin'),
    (4, 'owner');
SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE(MAX(id), 1), true) FROM roles;
-- rollback: DELETE FROM roles;

-- changeset albert:3-insert-role-scopes
INSERT INTO role_scope
SELECT 4, id FROM scopes; -- owner

INSERT INTO role_scope VALUES -- user
    (1, 1),
    (1, 2),
    (1, 6),
    (1, 9),
    (1, 13),
    (1, 15),
    (1, 19),
    (1, 21),
    (1, 27),
    (1, 28);

INSERT INTO role_scope VALUES -- maintenance
    (2, 1),
    (2, 2),
    (2, 6),
    (2, 9),
    (2, 10),
    (2, 14),
    (2, 27),
    (2, 28);

INSERT INTO role_scope VALUES -- admin
    (3, 1),
    (3, 2),
    (3, 3),
    (3, 4),
    (3, 6),
    (3, 7),
    (3, 8),
    (3, 9),
    (3, 10),
    (3, 11),
    (3, 12),
    (3, 14),
    (3, 17),
    (3, 18),
    (3, 19),
    (3, 20),
    (3, 22),
    (3, 23),
    (3, 25),
    (3, 26),
    (3, 27),
    (3, 28);
-- rollback DELETE FROM role_scope





