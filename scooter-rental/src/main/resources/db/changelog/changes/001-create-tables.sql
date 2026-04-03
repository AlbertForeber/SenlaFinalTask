-- db/changelog/changes/001-create-tables.sql

-- liquibase formatted sql

-- changeset albert:1-create-roles-table
CREATE TABLE IF NOT EXISTS roles(
    id SERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL
);
-- rollback DROP TABLE roles;

-- changeset albert:2-create-users-table
CREATE TABLE IF NOT EXISTS users(
    id SERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id INTEGER REFERENCES roles(id)
);
-- rollback DROP TABLE users;

-- changeset albert:3-create-user-details-table
CREATE TABLE IF NOT EXISTS user_details(
    user_id INTEGER REFERENCES users(id),
    full_name VARCHAR(255) NOT NULL
    date_of_birth DATE NOT NULL
    balance NUMERIC(10, 2) NOT NULL
    discount FLOAT
);
-- rollback DROP TABLE user_details;

-- changeset albert:4-create-scopes-table
CREATE TABLE IF NOT EXISTS scopes(
    id SERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL
);
-- rollback DROP TABLE scopes;

-- changeset albert:5-create-rent-spots-table
CREATE TABLE IF NOT EXISTS rent_spots(
    id SERIAL PRIMARY KEY,
    parent_id INTEGER REFERENCES rent_spots(id),
    name VARCHAR(256)
    area geography(Polygon, 4326) NOT NULL
);
-- rollback DROP TABLE rent_sports;

-- changeset albert:6-create-models-table
CREATE TABLE IF NOT EXISTS models(
    id SERIAL PRIMARY KEY,
    vendor VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL
);
-- rollback DROP TABLE models;

-- changeset albert:7-create-scooters-table
CREATE TABLE IF NOT EXISTS scooters(
    id SERIAL PRIMARY KEY,
    serial_no VARCHAR(255) UNIQUE NOT NULL,
    model_id INTEGER REFERENCES models(id),
    battery INTEGER NOT NULL CHECK (battery BETWEEN 0 AND 100),
    location geography(Point, 4326) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('FREE', 'OCCUPIED', 'MAINTENANCE'))
);
-- rollback DROP TABLE scooters;

-- changeset albert:8-create-tariffs-table
CREATE TABLE IF NOT EXISTS tariffs(
    id SERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    base_price INTEGER NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('HOURLY', 'SUBSCRIPTION'))
);
-- rollback DROP TABLE tariffs;

-- changeset albert:9-create-subscription-tariffs-table
CREATE TABLE IF NOT EXISTS subscription_tariffs(
    tariff_id INTEGER PRIMARY KEY REFERENCES tariffs(id),
    duration_days INTEGER NOT NULL
);
-- rollback DROP TABLE subscription_tariffs;

-- changeset albert:10-create-user-subscriptions-table
CREATE TABLE IF NOT EXISTS user_subscriptions(
    user_id INTEGER PRIMARY KEY REFERENCES users(id),
    tariff_id INTEGER REFERENCES tariffs(id),
    tariff_expiration_date DATE NOT NULL
);
-- rollback DROP TABLE user_subscriptions;

-- changeset albert:11-create-role-scope-table
CREATE TABLE IF NOT EXISTS role_scope(
    role_id INTEGER REFERENCES roles(id),
    scope_id INTEGER REFERENCES scopes(id),
    PRIMARY KEY (role_id, scope_id)
);
-- rollback DROP TABLE role_scope;

-- changeset albert:12-create-trips-table
CREATE TABLE IF NOT EXISTS trips(
    id SERIAL PRIMARY KEY,
    status VARCHAR(20) CHECK (status IN ('ONGOING', 'PAUSED', 'FINISHED')),
    scooter_id INTEGER REFERENCES scooter(id),
    user_id INTEGER REFERENCES users(id),
    started_at TIMESTAMP NOT NULL,

    -- Заполняется в конце
    distance FLOAT,
    duration_seconds INTEGER,
    total_price NUMERIC(10, 2)
);
-- rollback DROP TABLE trips;

-- changeset albert:13-create-trip-points
CREATE TABLE IF NOT EXISTS trip_points(
    trip_id INTEGER REFERENCES trips(id),
    location geography(Point, 4326) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (trip_id, created_at)
);
-- rollback DROP TABLE trip_points;