-- db/changelog/changes/999-add-test-values.sql

-- liquibase formatted sql

-- changeset albert:1-add-test-user
INSERT INTO users(username, password, role_id) VALUES
                    -- raw: test
    ('test', '$2a$12$ywD987n2HdO.rv/WPb/Yqevy6E7t0py6j1BD3PQAU7QE/zWmHbjTW', 4);
-- rollback: DELETE FROM users WHERE id=1;

-- changeset albert:2-add-test-user-details
INSERT INTO user_details VALUES
    (1, 'albertforeber2@gmail.com', '1999.9.9', 10000, 0.05);
-- rollback: DELETE FROM user_details WHERE id=1;

-- changeset albert:3-add-test-model
INSERT INTO models VALUES
    (1, 'test_vendor', 'test_model_name');
-- rollback DELETE FROM models WHERE id=1;

-- changeset albert:4-add-test-scooter
INSERT INTO scooters(serial_no, model_id, battery, location, status) VALUES
    ('test_serial', 1, 100, ST_GeomFromGeoJSON('
        {
          "coordinates": [
              0,
              0
          ],
          "type": "Point"
        }
    ')::geography, 'FREE'); -- будет заменен первым сообщением телеметрии
-- rollback DELETE FROM scooters WHERE id=1;

-- changeset albert:5-add-test-trip
INSERT INTO trips(status, scooter_id, user_id, started_at, price_at_start, interval_at_start, discount_at_start) VALUES
    ('FINISHED', 1, 1, '2026-04-18T8:00:00Z', 10, 1, 0.05);
-- rollback DELETE FROM trips WHERE id=1;

-- changeset albert:6-add-test-trip-points
INSERT INTO trip_points VALUES
    (1, ST_MakePoint(37.6485893, 55.7119318), '2026-01-01T00:00:00Z'),
    (1, ST_MakePoint(37.6467439, 55.7157273), '2026-01-01T00:00:05Z'),
    (1, ST_MakePoint(37.6480314, 55.7180721), '2026-01-01T00:00:10Z');
-- rollback DELETE FROM trip_points WHERE trip_id=1;

-- changeset albert:7-add-test-tariff-points
INSERT INTO tariffs VALUES
    (4, 'instant_test', 5000, NULL);
-- rollback DELETE FROM tariffs WHERE id=1;

-- changeset albert:8-add-test-subscription-tariffs
INSERT INTO subscription_tariffs VALUES
    (4, 0);
-- rollback DELETE FROM subscription_tariffs WHERE tariff_id=4;