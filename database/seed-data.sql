-- ============================================================
-- TripWise AI - Sample seed data
--
-- User accounts are created through Supabase Auth (sign up in the app, or
-- Supabase Dashboard -> Authentication -> Users -> Add user), NOT by
-- inserting rows here — Supabase owns password hashing/storage in its own
-- `auth.users` table, and this app mirrors the profile into `users`
-- automatically the first time that user calls the API
-- (see UserService.syncFromToken).
--
-- To seed a sample trip for a user you've already created:
--   1. Sign up in the app (or Supabase dashboard) and log in at least once,
--      so `users` has a row for them.
--   2. Replace 'demo@tripwise.ai' below with that user's real email.
--   3. Run this file.
-- ============================================================

-- Sample trip for the seeded user (rule-based, not AI-generated)
INSERT INTO trips (user_id, destination, days, budget, travel_type, interests, start_date, saved, ai_generated, created_at)
SELECT id, 'Bali, Indonesia', 5, 90000, 'Leisure',
       '["nature", "relaxation", "food"]'::jsonb,
       CURRENT_DATE + INTERVAL '30 days', TRUE, FALSE, NOW()
FROM users WHERE email = 'demo@tripwise.ai';

-- Itinerary days for the sample trip
INSERT INTO itinerary_days (trip_id, day_number, title)
SELECT t.id, gs.day_number,
       CASE
           WHEN gs.day_number = 1 THEN 'Day 1: Arrival & First Impressions of Bali, Indonesia'
           WHEN gs.day_number = t.days THEN 'Day ' || gs.day_number || ': Farewell to Bali, Indonesia'
           ELSE 'Day ' || gs.day_number || ': Discovering Bali, Indonesia'
       END
FROM trips t
CROSS JOIN generate_series(1, 5) AS gs(day_number)
WHERE t.destination = 'Bali, Indonesia'
  AND t.user_id = (SELECT id FROM users WHERE email = 'demo@tripwise.ai');

-- Sample activities for Day 1 of the sample trip
INSERT INTO activities (itinerary_day_id, description, category)
SELECT d.id, activity.description, activity.category
FROM itinerary_days d
JOIN trips t ON t.id = d.trip_id
CROSS JOIN (VALUES
    ('Explore the iconic landmarks of Bali, Indonesia', 'SIGHTSEEING'),
    ('Spend a relaxed afternoon at a beach or lakeside near Bali, Indonesia', 'EXPERIENCE'),
    ('Sample local street food and specialties in Bali, Indonesia', 'FOOD'),
    ('Dine at a highly-rated local restaurant in Bali, Indonesia', 'FOOD')
) AS activity(description, category)
WHERE d.day_number = 1 AND t.destination = 'Bali, Indonesia'
  AND t.user_id = (SELECT id FROM users WHERE email = 'demo@tripwise.ai');

-- Sample packing list for the sample trip
INSERT INTO packing_items (trip_id, name, category, checked, tip)
SELECT t.id, item.name, item.category, item.checked, item.tip
FROM trips t
CROSS JOIN (VALUES
    ('Passport / Government ID', 'DOCUMENTS', FALSE, 'Keep a photocopy and a digital backup.'),
    ('Travel insurance documents', 'DOCUMENTS', FALSE, 'Save a digital copy on your phone.'),
    ('Comfortable walking shoes', 'CLOTHING', FALSE, 'Break them in before the trip if new.'),
    ('Swimwear', 'CLOTHING', FALSE, NULL),
    ('Phone charger & power bank', 'ELECTRONICS', FALSE, NULL),
    ('Sunscreen', 'TOILETRIES', FALSE, 'Essential even on cloudy days.'),
    ('Basic first-aid kit', 'HEALTH', FALSE, NULL),
    ('Local currency / travel card', 'MISC', FALSE, 'Notify your bank of travel dates.')
) AS item(name, category, checked, tip)
WHERE t.destination = 'Bali, Indonesia'
  AND t.user_id = (SELECT id FROM users WHERE email = 'demo@tripwise.ai');
