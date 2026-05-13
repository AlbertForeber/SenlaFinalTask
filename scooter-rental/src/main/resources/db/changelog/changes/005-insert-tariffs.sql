-- db/changelog/changes/005-insert-tariffs.sql

-- liquibase formatted sql

-- changeset albert:1-insert-tariffs
INSERT INTO tariffs VALUES
    (1, 'By the minute', 10, 1),
    (2, 'By the hour', 400, 60),
    (3, 'Monthly subscription', 1800, NULL);
SELECT setval(pg_get_serial_sequence('tariffs', 'id'), COALESCE(MAX(id), 1), true) FROM tariffs;
-- rollback DELETE FROM subscription_tariffs

-- changeset albert:2-insert-subscriptions
INSERT INTO subscription_tariffs VALUES
    (3, 30);
-- rollback DELETE FROM subscription_tariffs;





