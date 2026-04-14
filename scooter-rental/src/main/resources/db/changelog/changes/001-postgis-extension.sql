-- db/changelog/changes/001-postgis-extension.sql

-- liquibase formatted sql

-- changeset albert:1-create-extension
CREATE EXTENSION IF NOT EXISTS postgis;
-- rollback DROP EXTENSION postgis;