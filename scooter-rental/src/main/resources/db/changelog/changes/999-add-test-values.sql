-- db/changelog/changes/999-add-test-values.sql

-- liquibase formatted sql

-- changeset albert:1-add-test-roles
INSERT INTO roles VALUES
    (1, 'test_role'),
    (2, 'second_test_empty_role');
-- rollback: DELETE FROM roles WHERE id BETWEEN 1 AND 2;

-- changeset albert:2-add-test-user
INSERT INTO users(username, password, role_id) VALUES
                    -- raw: test
    ('test', '$2a$12$ywD987n2HdO.rv/WPb/Yqevy6E7t0py6j1BD3PQAU7QE/zWmHbjTW', 1);
-- rollback: DELETE FROM users WHERE id=1;

-- changeset albert:3-add-test-user-details
INSERT INTO user_details VALUES
    (1, 'test_full_name', '1999.9.9', 1000, 0.05);
-- rollback: DELETE FROM user_details WHERE id=1;

-- changeset albert:4-add-test-rental-spots
INSERT INTO rent_spots(parent_id, name, area, is_zone) VALUES
    (NULL, 'root_test_spot', ST_GeomFromGeoJSON('{
  "coordinates": [
    [
      [
        37.3892212,
        55.788929
      ],
      [
        37.5059509,
        55.5837783
      ],
      [
        37.8135681,
        55.6442741
      ],
      [
        37.8382874,
        55.8221161
      ],
      [
        37.4935913,
        55.8945663
      ],
      [
        37.3892212,
        55.788929
      ]
    ]
  ],
  "type": "Polygon"}')::geography, false),
    (1, 'children_level_1_of_root_test_spot', ST_GeomFromGeoJSON('{
  "coordinates": [
    [
      [
        37.4441528,
        55.801281
      ],
      [
        37.4441528,
        55.7294301
      ],
      [
        37.5828552,
        55.7332964
      ],
      [
        37.6116943,
        55.7943334
      ],
      [
        37.4441528,
        55.801281
      ]
    ]
  ],
  "type": "Polygon"}')::geography, true);
-- rollback DELETE FROM rent_spots WHERE id BETWEEN 1 AND 2;

-- changeset albert:5-add-test-model
INSERT INTO models VALUES
    (1, 'test_vendor', 'test_model_name');
-- rollback DELETE FROM models WHERE id=1;

-- changeset albert:6-add-test-scooter
INSERT INTO scooters(serial_no, model_id, battery, location, status) VALUES
    ('test_serial', 1, 100, ST_GeomFromGeoJSON('
        {
          "coordinates": [
              37.7613831,
              55.6760357
          ],
          "type": "Point"
        }
    ')::geography, 'FREE');
-- rollback DELETE FROM scooters WHERE id=1;

-- changeset albert:7-add-test-trip
INSERT INTO trips(status, scooter_id, user_id, started_at, price_per_hour, discount_at_start) VALUES
    ('ONGOING', 1, 1, '2026-04-11T11:00:00Z', 600, 0.05);
-- rollback DELETE FROM trips WHERE id=1;

-- changeset albert:8-add-test-trip-points
INSERT INTO trip_points VALUES
    (1, ST_MakePoint(37.6485893, 55.7119318), '2026-01-01T00:00:00Z'),
    (1, ST_MakePoint(37.6467439, 55.7157273), '2026-01-01T00:00:05Z'),
    (1, ST_MakePoint(37.6480314, 55.7180721), '2026-01-01T00:00:10Z');
-- rollback DELETE FROM trip_points WHERE trip_id=1;

-- changeset albert:9-add-test-tariff-points
INSERT INTO tariffs(name, base_price, type) VALUES
    ('per_hour_test', 600, 'HOURLY');
-- rollback DELETE FROM tariffs WHERE id=1;

-- changeset albert:10-add-test-scopes
INSERT INTO scopes(name) VALUES
    ('profile:view'),
    ('profile:manage');
-- rollback DELETE FROM scopes WHERE id BETWEEN 1 AND 2;

-- changeset albert:10-add-test-role-scope
INSERT INTO role_scope VALUES
    (1, 1),
    (1, 2);
-- rollback DELETE FROM role_scope WHERE role_id=1;