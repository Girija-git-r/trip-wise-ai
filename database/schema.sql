-- ============================================================
-- TripWise AI - Smart Travel Planner
-- PostgreSQL schema
-- Note: Hibernate (spring.jpa.hibernate.ddl-auto=update) will also
-- create/update these tables automatically on backend startup.
-- This script is provided for manual setup / review.
--
-- If you already had a trips table from before the ai_generated column
-- existed, Hibernate's auto-update will fail to add it (Postgres refuses
-- a NOT NULL column on a table with existing rows). Run this once first:
--   ALTER TABLE trips ADD COLUMN IF NOT EXISTS ai_generated BOOLEAN NOT NULL DEFAULT FALSE;
-- ============================================================

CREATE DATABASE tripwise_ai;
-- \c tripwise_ai

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(180) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS trips (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    destination     VARCHAR(160) NOT NULL,
    days            INTEGER NOT NULL CHECK (days > 0),
    budget          DOUBLE PRECISION NOT NULL CHECK (budget > 0),
    travel_type     VARCHAR(40) NOT NULL,
    interests       JSONB NOT NULL DEFAULT '[]',
    start_date      DATE,
    saved           BOOLEAN NOT NULL DEFAULT FALSE,
    ai_generated    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS itinerary_days (
    id              BIGSERIAL PRIMARY KEY,
    trip_id         BIGINT NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    day_number      INTEGER NOT NULL,
    title           VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS activities (
    id                  BIGSERIAL PRIMARY KEY,
    itinerary_day_id    BIGINT NOT NULL REFERENCES itinerary_days(id) ON DELETE CASCADE,
    description         TEXT NOT NULL,
    category            VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS packing_items (
    id              BIGSERIAL PRIMARY KEY,
    trip_id         BIGINT NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    name            VARCHAR(160) NOT NULL,
    category        VARCHAR(40) NOT NULL,
    checked         BOOLEAN NOT NULL DEFAULT FALSE,
    tip             TEXT
);

CREATE INDEX IF NOT EXISTS idx_trips_user_id ON trips(user_id);
CREATE INDEX IF NOT EXISTS idx_itinerary_days_trip_id ON itinerary_days(trip_id);
CREATE INDEX IF NOT EXISTS idx_activities_itinerary_day_id ON activities(itinerary_day_id);
CREATE INDEX IF NOT EXISTS idx_packing_items_trip_id ON packing_items(trip_id);
