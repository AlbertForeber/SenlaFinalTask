-- db/changelog/changes/002-create-tables.sql

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
    role_id INTEGER NOT NULL REFERENCES roles(id)
);
CREATE INDEX idx_users_username ON users(username);
-- rollback DROP TABLE users;

-- changeset albert:3-create-user-details-table
CREATE TABLE IF NOT EXISTS user_details(
    user_id INTEGER PRIMARY KEY REFERENCES users(id),
    full_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    balance DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(3, 2) CHECK (discount BETWEEN 0 AND 1)
);
-- rollback DROP TABLE user_details;

-- changeset albert:4-create-scopes-table
CREATE TABLE IF NOT EXISTS scopes(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
-- rollback DROP TABLE scopes;

-- changeset albert:5-create-rent-spots-table
CREATE TABLE IF NOT EXISTS rent_spots(
    id SERIAL PRIMARY KEY,
    parent_id INTEGER REFERENCES rent_spots(id),
    name VARCHAR(255) NOT NULL,
    area geography(Polygon, 4326) NOT NULL,
    is_zone BOOLEAN NOT NULL
);
CREATE INDEX idx_rent_spots_area ON rent_spots USING GIST(area);
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
    model_id INTEGER NOT NULL REFERENCES models(id),
    battery INTEGER NOT NULL CHECK (battery BETWEEN 0 AND 100),
    location geography(Point, 4326) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('FREE', 'OCCUPIED', 'MAINTENANCE'))
);
CREATE INDEX idx_scooters_location ON scooters USING GIST(location);
-- rollback DROP TABLE scooters;

-- changeset albert:8-create-tariffs-table
CREATE TABLE IF NOT EXISTS tariffs(
    id SERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
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
    tariff_id INTEGER NOT NULL REFERENCES tariffs(id),
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
    scooter_id INTEGER NOT NULL REFERENCES scooters(id),
    user_id INTEGER NOT NULL REFERENCES users(id),
    started_at TIMESTAMP NOT NULL,

    -- Историческая денормализация
    price_per_hour DECIMAL(10, 2) NOT NULL,
    discount_at_start DECIMAL(3, 2) CHECK (discount_at_start BETWEEN 0 AND 1),

    -- Заполняется в конце
    distance FLOAT,
    duration_seconds INTEGER,
    total_price NUMERIC(10, 2)
);
-- rollback DROP TABLE trips;

-- changeset albert:13-create-trip-points-table
CREATE TABLE IF NOT EXISTS trip_points(
    trip_id INTEGER REFERENCES trips(id),
    location geography(Point, 4326) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (trip_id, created_at)
);
-- rollback DROP TABLE trip_points;

-- changeset albert:14-create-refresh-token-table
CREATE TABLE IF NOT EXISTS refresh_tokens(
    id SERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE, -- автоматически создается index (unique)
    user_id INTEGER NOT NULL REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    used BOOLEAN NOT NULL,
    revoked BOOLEAN NOT NULL,
    replaced_by_token VARCHAR(255) REFERENCES refresh_tokens(token)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
-- rollback DROP TABLE refresh_tokens;