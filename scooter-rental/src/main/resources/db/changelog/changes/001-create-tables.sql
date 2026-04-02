-- db/changelog/changes/001-create-tables.sql

-- liquibase formatted sql

-- changeset albert:1-create-roles-table
CREATE TABLE IF NOT EXISTS roles(
    id SERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL
)
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
)
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
    area Geography(Polygon, 4326) NOT NULL
)
