-- ============================================================
-- TripWise AI - Smart Travel Planner
-- PostgreSQL schema (Supabase-hosted)
--
-- Auth is entirely handled by Supabase Auth (auth.users) — this schema only
-- adds a `users` profile table keyed by that same UUID, plus the app's own
-- trip data. Run this in the Supabase SQL Editor (or let Hibernate's
-- ddl-auto=update create it on first backend boot against the same DB).
--
-- Note: this app's backend connects with the Postgres role from Supabase's
-- connection string (not the anon/public API), so Row Level Security is not
-- required for it to function — the Spring Boot service is the only thing
-- that touches these tables directly. RLS is worth adding later only if you
-- plan to query these tables from the frontend via supabase-js directly.
--
-- If you already had a trips table from before the ai_generated column
-- existed, Hibernate's auto-update will fail to add it (Postgres refuses
-- a NOT NULL column on a table with existing rows). Run this once first:
--   ALTER TABLE trips ADD COLUMN IF NOT EXISTS ai_generated BOOLEAN NOT NULL DEFAULT FALSE;
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(180) NOT NULL UNIQUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS trips (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
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
